package com.expensesplitter.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesplitter.dto.ParseExpenseRequest;
import com.expensesplitter.dto.ParsedExpense;
import com.expensesplitter.security.CurrentUser;
import com.expensesplitter.service.ExpenseAiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups/{groupId}/ai")
public class AiController {

    private final ExpenseAiService expenseAiService;
    private final CurrentUser currentUser;

    public AiController(ExpenseAiService expenseAiService, CurrentUser currentUser) {
        this.expenseAiService = expenseAiService;
        this.currentUser = currentUser;
    }

    @PostMapping("/parse-expense")
    public ParsedExpense parse(@PathVariable Long groupId, @Valid @RequestBody ParseExpenseRequest request) {
        return expenseAiService.parse(groupId, request.text(), currentUser.id());
    }
}