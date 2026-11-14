package com.expensesplitter.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long groupId, Long fromUserId, Long toUserId,
                              BigDecimal amount, LocalDateTime createdAt) {
}