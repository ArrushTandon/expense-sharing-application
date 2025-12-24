package com.expensesharing.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BalanceSimplifier {

    public List<Transaction> simplifyBalances(Map<UUID, BigDecimal> balances) {
        List<UserBalance> creditors = new ArrayList<>();
        List<UserBalance> debtors = new ArrayList<>();

        balances.forEach((userId, balance) -> {
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalance(userId, balance));
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new UserBalance(userId, balance.abs()));
            }
        });

        creditors.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        debtors.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        List<Transaction> transactions = new ArrayList<>();
        int i = 0, j = 0;

        while (i < creditors.size() && j < debtors.size()) {
            UserBalance creditor = creditors.get(i);
            UserBalance debtor = debtors.get(j);

            BigDecimal amount = creditor.getAmount().min(debtor.getAmount());
            transactions.add(new Transaction(debtor.getUserId(), creditor.getUserId(), amount));

            creditor.setAmount(creditor.getAmount().subtract(amount));
            debtor.setAmount(debtor.getAmount().subtract(amount));

            if (creditor.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (debtor.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        return transactions;
    }

    @Data
    @AllArgsConstructor
    public static class UserBalance {
        private UUID userId;
        private BigDecimal amount;
    }

    @Data
    @AllArgsConstructor
    public static class Transaction {
        private UUID fromUser;
        private UUID toUser;
        private BigDecimal amount;
    }
}