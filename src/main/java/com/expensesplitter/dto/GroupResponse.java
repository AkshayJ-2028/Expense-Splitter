package com.expensesplitter.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupResponse(Long id, String name, Long createdBy, LocalDateTime createdAt,
                            List<MemberResponse> members) {
}