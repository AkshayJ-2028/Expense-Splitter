package com.expensesplitter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.security.CurrentUser;
import com.expensesplitter.service.BalanceService;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class BalanceController {

    private final BalanceService balanceService;
    private final CurrentUser currentUser;

    public BalanceController(BalanceService balanceService, CurrentUser currentUser) {
        this.balanceService = balanceService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public BalanceResponse balances(@PathVariable Long groupId) {
        return balanceService.balances(groupId, currentUser.id());
    }
}