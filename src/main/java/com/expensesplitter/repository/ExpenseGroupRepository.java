package com.expensesplitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensesplitter.entity.ExpenseGroup;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {
}