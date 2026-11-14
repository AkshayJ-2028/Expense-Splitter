package com.expensesplitter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.entity.Payment;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final GroupMemberRepository members;
    private final GroupService groupService;
    private final BalanceService balanceService;

    public PaymentService(PaymentRepository payments, GroupMemberRepository members,
                          GroupService groupService, BalanceService balanceService) {
        this.payments = payments;
        this.members = members;
        this.groupService = groupService;
        this.balanceService = balanceService;
    }

    /** The logged-in user records that they paid someone in the group. */
    @Transactional
    public BalanceResponse record(Long groupId, Long fromUserId, Long toUserId, BigDecimal amount) {
        groupService.ensureMember(groupId, fromUserId);
        if (fromUserId.equals(toUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot pay yourself");
        }
        if (!members.existsByGroupIdAndUserId(groupId, toUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The receiver is not in this group");
        }

        Payment payment = new Payment();
        payment.setGroupId(groupId);
        payment.setFromUserId(fromUserId);
        payment.setToUserId(toUserId);
        payment.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        payments.save(payment);

        return balanceService.balances(groupId, fromUserId);
    }
}