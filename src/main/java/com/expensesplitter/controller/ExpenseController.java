package com.expensesplitter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.expensesplitter.dto.ExpenseRequest;
import com.expensesplitter.dto.ExpenseResponse;
import com.expensesplitter.security.CurrentUser;
import com.expensesplitter.service.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUser currentUser;

    public ExpenseController(ExpenseService expenseService, CurrentUser currentUser) {
        this.expenseService = expenseService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@PathVariable Long groupId, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(groupId, request, currentUser.id());
    }

    @GetMapping
    public List<ExpenseResponse> list(@PathVariable Long groupId) {
        return expenseService.list(groupId, currentUser.id());
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse get(@PathVariable Long groupId, @PathVariable Long expenseId) {
        return expenseService.get(groupId, expenseId, currentUser.id());
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long groupId, @PathVariable Long expenseId) {
        expenseService.delete(groupId, expenseId, currentUser.id());
    }
}