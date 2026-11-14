package com.expensesplitter.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.expensesplitter.dto.SplitInput;
import com.expensesplitter.entity.SplitType;

class SplitCalculatorTest {

    private final SplitCalculator calculator = new SplitCalculator();

    private SplitInput in(long userId, String value) {
        return new SplitInput(userId, value == null ? null : new BigDecimal(value));
    }

    private BigDecimal sum(Map<Long, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void equalSplitGivesExtraPaiseToFirstPeople() {
        Map<Long, BigDecimal> shares = calculator.calculate(new BigDecimal("100.00"), SplitType.EQUAL,
                List.of(in(1, null), in(2, null), in(3, null)));

        assertThat(shares.get(1L)).isEqualByComparingTo("33.34");
        assertThat(shares.get(2L)).isEqualByComparingTo("33.33");
        assertThat(shares.get(3L)).isEqualByComparingTo("33.33");
        assertThat(sum(shares)).isEqualByComparingTo("100.00");
    }

    @Test
    void equalSplitBetweenTwoPeople() {
        Map<Long, BigDecimal> shares = calculator.calculate(new BigDecimal("1000"), SplitType.EQUAL,
                List.of(in(1, null), in(2, null)));

        assertThat(shares.get(1L)).isEqualByComparingTo("500.00");
        assertThat(shares.get(2L)).isEqualByComparingTo("500.00");
    }

    @Test
    void percentSplitCalculatesShares() {
        Map<Long, BigDecimal> shares = calculator.calculate(new BigDecimal("900"), SplitType.PERCENT,
                List.of(in(1, "70"), in(2, "30")));

        assertThat(shares.get(1L)).isEqualByComparingTo("630.00");
        assertThat(shares.get(2L)).isEqualByComparingTo("270.00");
    }

    @Test
    void percentSplitAlwaysAddsUpToTotalDespiteRounding() {
        Map<Long, BigDecimal> shares = calculator.calculate(new BigDecimal("100.00"), SplitType.PERCENT,
                List.of(in(1, "33.33"), in(2, "33.33"), in(3, "33.34")));

        assertThat(sum(shares)).isEqualByComparingTo("100.00");
    }

    @Test
    void percentNotSummingTo100IsRejected() {
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("100"), SplitType.PERCENT,
                List.of(in(1, "60"), in(2, "30"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("100");
    }

    @Test
    void exactSplitUsesGivenAmounts() {
        Map<Long, BigDecimal> shares = calculator.calculate(new BigDecimal("500"), SplitType.EXACT,
                List.of(in(1, "350"), in(2, "150")));

        assertThat(shares.get(1L)).isEqualByComparingTo("350.00");
        assertThat(shares.get(2L)).isEqualByComparingTo("150.00");
    }

    @Test
    void exactSplitWithWrongTotalIsRejected() {
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("500"), SplitType.EXACT,
                List.of(in(1, "300"), in(2, "100"))))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void sameUserListedTwiceIsRejected() {
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("100"), SplitType.EQUAL,
                List.of(in(1, null), in(1, null))))
                .isInstanceOf(ResponseStatusException.class);
    }
}