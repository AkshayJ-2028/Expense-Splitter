package com.expensesplitter.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensesplitter.entity.ExpenseSplit;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    List<ExpenseSplit> findByExpenseId(Long expenseId);
    List<ExpenseSplit> findByExpenseIdIn(Collection<Long> expenseIds);
    void deleteByExpenseId(Long expenseId);
    void deleteByExpenseIdIn(Collection<Long> expenseIds);
}