package com.expensesplitter.dto;

import java.math.BigDecimal;
import java.util.List;

public record ParsedExpense(String description, BigDecimal amount, String category, Long paidBy,
                            List<Long> participantIds, List<String> unmatchedNames, String splitType) {
}