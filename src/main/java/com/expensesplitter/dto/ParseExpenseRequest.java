package com.expensesplitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParseExpenseRequest(
        @NotBlank(message = "Text is required")
        @Size(max = 300, message = "Keep it under 300 characters")
        String text) {
}