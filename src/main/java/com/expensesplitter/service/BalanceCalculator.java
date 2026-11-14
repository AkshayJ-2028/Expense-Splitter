package com.expensesplitter.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.expensesplitter.dto.Settlement;

/**
 * Takes each person's net balance (positive = is owed money, negative = owes money)
 * and produces a short list of payments that settles everyone.
 */
@Component
public class BalanceCalculator {

    private static class Entry {
        final Long userId;
        BigDecimal amount;

        Entry(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }

    public List<Settlement> settle(Map<Long, BigDecimal> net, Map<Long, String> names) {
        List<Entry> creditors = new ArrayList<>();
        List<Entry> debtors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> e : net.entrySet()) {
            int sign = e.getValue().signum();
            if (sign > 0) {
                creditors.add(new Entry(e.getKey(), e.getValue()));
            } else if (sign < 0) {
                debtors.add(new Entry(e.getKey(), e.getValue().negate()));
            }
        }
        creditors.sort(Comparator.comparing((Entry x) -> x.amount).reversed());
        debtors.sort(Comparator.comparing((Entry x) -> x.amount).reversed());

        List<Settlement> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Entry debtor = debtors.get(i);
            Entry creditor = creditors.get(j);
            BigDecimal pay = debtor.amount.min(creditor.amount);

            result.add(new Settlement(
                    debtor.userId, names.getOrDefault(debtor.userId, "Unknown"),
                    creditor.userId, names.getOrDefault(creditor.userId, "Unknown"),
                    pay));

            debtor.amount = debtor.amount.subtract(pay);
            creditor.amount = creditor.amount.subtract(pay);
            if (debtor.amount.signum() == 0) {
                i++;
            }
            if (creditor.amount.signum() == 0) {
                j++;
            }
        }
        return result;
    }
}