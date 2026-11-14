package com.expensesplitter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.expensesplitter.dto.SplitInput;
import com.expensesplitter.entity.SplitType;

/**
 * Turns "total amount + split type + people" into each person's exact share.
 * Shares are always in rupees and paise (2 decimals) and always add up exactly to the total.
 */
@Component
public class SplitCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public Map<Long, BigDecimal> calculate(BigDecimal total, SplitType type, List<SplitInput> inputs) {
        total = total.setScale(2, RoundingMode.HALF_UP);
        ensureNoDuplicates(inputs);

        return switch (type) {
            case EQUAL -> equal(total, inputs);
            case PERCENT -> percent(total, inputs);
            case EXACT -> exact(total, inputs);
        };
    }

    // EQUAL: 100.00 between 3 people -> 33.34, 33.33, 33.33 (extra paise go to the first people)
    private Map<Long, BigDecimal> equal(BigDecimal total, List<SplitInput> inputs) {
        int n = inputs.size();
        long totalPaise = total.movePointRight(2).longValueExact();
        long base = totalPaise / n;
        long remainder = totalPaise % n;

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            long paise = base + (i < remainder ? 1 : 0);
            result.put(inputs.get(i).userId(), BigDecimal.valueOf(paise, 2));
        }
        return result;
    }

    // PERCENT: percentages must add up to 100; rounding leftovers are fixed on the last person
    private Map<Long, BigDecimal> percent(BigDecimal total, List<SplitInput> inputs) {
        requireValues(inputs);
        BigDecimal sum = inputs.stream().map(SplitInput::value).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(HUNDRED) != 0) {
            throw bad("Percentages must add up to 100 (they add up to " + sum.stripTrailingZeros().toPlainString() + ")");
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (int i = 0; i < inputs.size(); i++) {
            SplitInput in = inputs.get(i);
            BigDecimal share;
            if (i == inputs.size() - 1) {
                share = total.subtract(assigned);
            } else {
                share = total.multiply(in.value()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                assigned = assigned.add(share);
            }
            result.put(in.userId(), share);
        }
        return result;
    }

    // EXACT: the amounts must add up to the total
    private Map<Long, BigDecimal> exact(BigDecimal total, List<SplitInput> inputs) {
        requireValues(inputs);
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (SplitInput in : inputs) {
            BigDecimal share = in.value().setScale(2, RoundingMode.HALF_UP);
            sum = sum.add(share);
            result.put(in.userId(), share);
        }
        if (sum.compareTo(total) != 0) {
            throw bad("Amounts must add up to " + total.toPlainString() + " (they add up to " + sum.toPlainString() + ")");
        }
        return result;
    }

    private void requireValues(List<SplitInput> inputs) {
        for (SplitInput in : inputs) {
            if (in.value() == null || in.value().signum() < 0) {
                throw bad("Every person needs a value of 0 or more");
            }
        }
    }

    private void ensureNoDuplicates(List<SplitInput> inputs) {
        Set<Long> seen = new HashSet<>();
        for (SplitInput in : inputs) {
            if (!seen.add(in.userId())) {
                throw bad("The same person is listed twice in the split");
            }
        }
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}