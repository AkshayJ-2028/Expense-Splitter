package com.expensesplitter.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.dto.MemberBalance;
import com.expensesplitter.dto.Settlement;
import com.expensesplitter.entity.Expense;
import com.expensesplitter.entity.ExpenseSplit;
import com.expensesplitter.entity.GroupMember;
import com.expensesplitter.entity.Payment;
import com.expensesplitter.entity.User;
import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.ExpenseSplitRepository;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.PaymentRepository;
import com.expensesplitter.repository.UserRepository;

@Service
public class BalanceService {

    private final ExpenseRepository expenses;
    private final ExpenseSplitRepository splits;
    private final GroupMemberRepository members;
    private final UserRepository users;
    private final PaymentRepository payments;
    private final GroupService groupService;
    private final BalanceCalculator calculator;

    public BalanceService(ExpenseRepository expenses, ExpenseSplitRepository splits,
                          GroupMemberRepository members, UserRepository users,
                          PaymentRepository payments, GroupService groupService,
                          BalanceCalculator calculator) {
        this.expenses = expenses;
        this.splits = splits;
        this.members = members;
        this.users = users;
        this.payments = payments;
        this.groupService = groupService;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public BalanceResponse balances(Long groupId, Long userId) {
        groupService.ensureMember(groupId, userId);

        // Everyone starts at 0
        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        List<GroupMember> groupMembers = members.findByGroupId(groupId);
        for (GroupMember m : groupMembers) {
            net.put(m.getUserId(), BigDecimal.ZERO);
        }

        // Payer is owed the full amount; each person in the split owes their share
        List<Expense> groupExpenses = expenses.findByGroupId(groupId);
        for (Expense e : groupExpenses) {
            net.merge(e.getPaidBy(), e.getAmount(), BigDecimal::add);
        }
        if (!groupExpenses.isEmpty()) {
            List<Long> expenseIds = groupExpenses.stream().map(Expense::getId).toList();
            for (ExpenseSplit s : splits.findByExpenseIdIn(expenseIds)) {
                net.merge(s.getUserId(), s.getShareAmount().negate(), BigDecimal::add);
            }
        }

        // Recorded payments: the payer owes less, the receiver is owed less
        for (Payment p : payments.findByGroupId(groupId)) {
            net.merge(p.getFromUserId(), p.getAmount(), BigDecimal::add);
            net.merge(p.getToUserId(), p.getAmount().negate(), BigDecimal::add);
        }

        Map<Long, String> names = new HashMap<>();
        for (User u : users.findAllById(net.keySet())) {
            names.put(u.getId(), u.getName());
        }

        List<MemberBalance> balances = net.entrySet().stream()
                .map(en -> new MemberBalance(en.getKey(), names.getOrDefault(en.getKey(), "Unknown"), en.getValue()))
                .toList();
        List<Settlement> settlements = calculator.settle(net, names);

        return new BalanceResponse(groupId, balances, settlements);
    }
}