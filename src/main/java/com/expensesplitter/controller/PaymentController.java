package com.expensesplitter.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.dto.PaymentRequest;
import com.expensesplitter.security.CurrentUser;
import com.expensesplitter.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups/{groupId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUser currentUser;

    public PaymentController(PaymentService paymentService, CurrentUser currentUser) {
        this.paymentService = paymentService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public BalanceResponse record(@PathVariable Long groupId, @Valid @RequestBody PaymentRequest request) {
        return paymentService.record(groupId, currentUser.id(), request.toUserId(), request.amount());
    }
}