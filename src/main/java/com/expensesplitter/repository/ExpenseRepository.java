package com.expensesplitter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensesplitter.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupIdOrderByExpenseDateDescIdDesc(Long groupId);
    List<Expense> findByGroupId(Long groupId);
    void deleteByGroupId(Long groupId);
}