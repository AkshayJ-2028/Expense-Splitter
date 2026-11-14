package com.expensesplitter.dto;

import java.math.BigDecimal;

public record Settlement(Long fromUserId, String fromName,
Long toUserId, String toName,
 BigDecimal amount) {
}