package com.expensesplitter.dto;

public record AuthResponse(String token, String name, String email) {
}