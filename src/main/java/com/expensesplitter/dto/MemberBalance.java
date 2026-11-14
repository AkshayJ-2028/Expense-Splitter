package com.expensesplitter.dto;

import java.math.BigDecimal;

public record MemberBalance(Long userId, String name, BigDecimal net) {
}