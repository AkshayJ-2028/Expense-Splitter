package com.expensesplitter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesplitter.dto.ExpenseRequest;
import com.expensesplitter.dto.ExpenseResponse;
import com.expensesplitter.dto.SplitInput;
import com.expensesplitter.dto.SplitResponse;
import com.expensesplitter.entity.Expense;
import com.expensesplitter.entity.ExpenseGroup;
import com.expensesplitter.entity.ExpenseSplit;
import com.expensesplitter.entity.User;
import com.expensesplitter.repository.ExpenseGroupRepository;
import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.ExpenseSplitRepository;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.UserRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;
    private final ExpenseSplitRepository splits;
    private final GroupMemberRepository members;
    private final ExpenseGroupRepository groups;
    private final UserRepository users;
    private final GroupService groupService;
    private final SplitCalculator calculator;

    public ExpenseService(ExpenseRepository expenses, ExpenseSplitRepository splits,
                          GroupMemberRepository members, ExpenseGroupRepository groups,
                          UserRepository users, GroupService groupService,
                          SplitCalculator calculator) {
        this.expenses = expenses;
        this.splits = splits;
        this.members = members;
        this.groups = groups;
        this.users = users;
        this.groupService = groupService;
        this.calculator = calculator;
    }

    @Transactional
    public ExpenseResponse create(Long groupId, ExpenseRequest request, Long userId) {
        groupService.ensureMember(groupId, userId);
        requireMember(groupId, request.paidBy(), "The payer is not in this group");
        for (SplitInput in : request.splits()) {
            requireMember(groupId, in.userId(), "Everyone in the split must be a member of this group");
        }

        Map<Long, BigDecimal> shares =
                calculator.calculate(request.amount(), request.splitType(), request.splits());

        Expense expense = new Expense();
        expense.setGroupId(groupId);
        expense.setPaidBy(request.paidBy());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setDescription(request.description().trim());
        expense.setCategory(request.category() == null || request.category().isBlank()
                ? null : request.category().trim());
        expense.setSplitType(request.splitType());
        expense.setExpenseDate(request.expenseDate() == null ? LocalDate.now() : request.expenseDate());
        expense = expenses.save(expense);

        for (Map.Entry<Long, BigDecimal> share : shares.entrySet()) {
            splits.save(new ExpenseSplit(expense.getId(), share.getKey(), share.getValue()));
        }
        return toResponses(List.of(expense)).get(0);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Long groupId, Long userId) {
        groupService.ensureMember(groupId, userId);
        return toResponses(expenses.findByGroupIdOrderByExpenseDateDescIdDesc(groupId));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long groupId, Long expenseId, Long userId) {
        groupService.ensureMember(groupId, userId);
        return toResponses(List.of(findInGroup(groupId, expenseId))).get(0);
    }

    @Transactional
    public void delete(Long groupId, Long expenseId, Long userId) {
        groupService.ensureMember(groupId, userId);
        Expense expense = findInGroup(groupId, expenseId);
        ExpenseGroup group = groups.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        boolean isPayer = expense.getPaidBy().equals(userId);
        boolean isGroupCreator = group.getCreatedBy().equals(userId);
        if (!isPayer && !isGroupCreator) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the payer or the group creator can delete this expense");
        }
        splits.deleteByExpenseId(expenseId);
        expenses.delete(expense);
    }

    private Expense findInGroup(Long groupId, Long expenseId) {
        Expense expense = expenses.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
        if (!expense.getGroupId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }
        return expense;
    }

    private void requireMember(Long groupId, Long userId, String message) {
        if (!members.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /** Builds responses for many expenses using only 3 queries (no query per expense). */
    private List<ExpenseResponse> toResponses(List<Expense> expenseList) {
        if (expenseList.isEmpty()) {
            return List.of();
        }
        List<Long> expenseIds = expenseList.stream().map(Expense::getId).toList();
        Map<Long, List<ExpenseSplit>> splitsByExpense = splits.findByExpenseIdIn(expenseIds).stream()
                .collect(Collectors.groupingBy(ExpenseSplit::getExpenseId));

        Set<Long> userIds = new HashSet<>();
        for (Expense e : expenseList) {
            userIds.add(e.getPaidBy());
            for (ExpenseSplit s : splitsByExpense.getOrDefault(e.getId(), List.of())) {
                userIds.add(s.getUserId());
            }
        }
        Map<Long, User> usersById = users.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return expenseList.stream().map(e -> {
            List<SplitResponse> splitList = splitsByExpense.getOrDefault(e.getId(), List.of()).stream()
                    .map(s -> new SplitResponse(s.getUserId(), nameOf(usersById, s.getUserId()), s.getShareAmount()))
                    .toList();
            return new ExpenseResponse(e.getId(), e.getGroupId(), e.getPaidBy(),
                    nameOf(usersById, e.getPaidBy()), e.getAmount(), e.getDescription(),
                    e.getCategory(), e.getSplitType(), e.getExpenseDate(), splitList);
        }).toList();
    }

    private String nameOf(Map<Long, User> usersById, Long userId) {
        User user = usersById.get(userId);
        return user == null ? "Unknown" : user.getName();
    }
}