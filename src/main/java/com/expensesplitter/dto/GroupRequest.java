package com.expensesplitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequest(
        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Group name must be at most 100 characters")
        String name) {
}