package com.expensesplitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.expensesplitter.entity.SplitType;

public record ExpenseResponse(Long id, Long groupId, Long paidBy, String paidByName,
BigDecimal amount, String description, String category,
SplitType splitType, LocalDate expenseDate,
List<SplitResponse> splits) {
}