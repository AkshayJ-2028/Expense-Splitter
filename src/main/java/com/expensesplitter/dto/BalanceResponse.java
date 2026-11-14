package com.expensesplitter.dto;

import java.util.List;

public record BalanceResponse(Long groupId, List<MemberBalance> balances, List<Settlement> settlements) {
}