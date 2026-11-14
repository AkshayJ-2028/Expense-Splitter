package com.expensesplitter.dto;

import java.math.BigDecimal;

public record SplitResponse(Long userId, String name, BigDecimal shareAmount) {
}