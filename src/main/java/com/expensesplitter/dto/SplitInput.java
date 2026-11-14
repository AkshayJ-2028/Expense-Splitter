package com.expensesplitter.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record SplitInput(
        @NotNull(message = "User id is required")
        Long userId,
        BigDecimal value) {
}