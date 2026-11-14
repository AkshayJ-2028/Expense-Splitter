package com.expensesplitter.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.expensesplitter.dto.Settlement;

class BalanceCalculatorTest {

    private final BalanceCalculator calculator = new BalanceCalculator();

    private final Map<Long, String> names = Map.of(1L, "Akshay", 2L, "Rahul", 3L, "Priya");

    @Test
    void twoPeopleOneSettlement() {
        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        net.put(1L, new BigDecimal("20.00"));
        net.put(2L, new BigDecimal("-20.00"));

        List<Settlement> result = calculator.settle(net, names);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fromUserId()).isEqualTo(2L);
        assertThat(result.get(0).toUserId()).isEqualTo(1L);
        assertThat(result.get(0).amount()).isEqualByComparingTo("20.00");
    }

    @Test
    void everyoneSettledGivesNoPayments() {
        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        net.put(1L, BigDecimal.ZERO);
        net.put(2L, BigDecimal.ZERO);

        assertThat(calculator.settle(net, names)).isEmpty();
    }

    @Test
    void oneCreditorTwoDebtors() {
        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        net.put(1L, new BigDecimal("50.00"));
        net.put(2L, new BigDecimal("-30.00"));
        net.put(3L, new BigDecimal("-20.00"));

        List<Settlement> result = calculator.settle(net, names);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.toUserId().equals(1L));
        BigDecimal total = result.stream().map(Settlement::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("50.00");
    }

    @Test
    void paymentsAlwaysCoverTheFullDebt() {
        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        net.put(1L, new BigDecimal("70.00"));
        net.put(2L, new BigDecimal("-40.00"));
        net.put(3L, new BigDecimal("-30.00"));

        List<Settlement> result = calculator.settle(net, names);

        BigDecimal paidByRahul = result.stream().filter(s -> s.fromUserId().equals(2L))
                .map(Settlement::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidByPriya = result.stream().filter(s -> s.fromUserId().equals(3L))
                .map(Settlement::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(paidByRahul).isEqualByComparingTo("40.00");
        assertThat(paidByPriya).isEqualByComparingTo("30.00");
    }
}