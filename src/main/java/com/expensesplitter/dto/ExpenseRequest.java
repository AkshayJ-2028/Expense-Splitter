package com.expensesplitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.expensesplitter.entity.SplitType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpenseRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        @Size(max = 200, message = "Description must be at most 200 characters")
        String description,

        @Size(max = 50, message = "Category must be at most 50 characters")
        String category,

        @NotNull(message = "Paid by is required")
        Long paidBy,

        @NotNull(message = "Split type is required")
        SplitType splitType,

        LocalDate expenseDate,

        @NotNull(message = "Splits are required")
        @Size(min = 1, message = "At least one person must share the expense")
        @Valid
        List<SplitInput> splits) {
}