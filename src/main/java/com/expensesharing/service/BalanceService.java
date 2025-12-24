package com.expensesharing.service;

import com.expensesharing.dto.response.BalanceResponse;
import com.expensesharing.dto.response.SimplifiedBalanceResponse;
import com.expensesharing.entity.Expense;
import com.expensesharing.entity.ExpenseSplit;
import com.expensesharing.entity.Settlement;
import com.expensesharing.repository.ExpenseSplitRepository;
import com.expensesharing.repository.SettlementRepository;
import com.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseSplitRepository splitRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final BalanceSimplifier balanceSimplifier;

    public BalanceResponse getUserBalances(UUID userId) {
        Map<UUID, BigDecimal> netBalances = calculateNetBalances(userId);

        List<BalanceResponse.BalanceDetail> owes = new ArrayList<>();
        List<BalanceResponse.BalanceDetail> owedBy = new ArrayList<>();
        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalOwing = BigDecimal.ZERO;

        for (Map.Entry<UUID, BigDecimal> entry : netBalances.entrySet()) {
            BigDecimal amount = entry.getValue();
            String userName = userRepository.findById(entry.getKey())
                    .map(u -> u.getName())
                    .orElse("Unknown");

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                owes.add(BalanceResponse.BalanceDetail.builder()
                        .userId(entry.getKey())
                        .userName(userName)
                        .amount(amount.abs())
                        .build());
                totalOwing = totalOwing.add(amount.abs());
            } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
                owedBy.add(BalanceResponse.BalanceDetail.builder()
                        .userId(entry.getKey())
                        .userName(userName)
                        .amount(amount)
                        .build());
                totalOwed = totalOwed.add(amount);
            }
        }

        return BalanceResponse.builder()
                .owes(owes)
                .owedBy(owedBy)
                .netBalance(totalOwed.subtract(totalOwing))
                .build();
    }

    public SimplifiedBalanceResponse getSimplifiedGroupBalances(UUID groupId) {
        Map<UUID, BigDecimal> userBalances = calculateGroupBalances(groupId);
        List<BalanceSimplifier.Transaction> transactions = balanceSimplifier.simplifyBalances(userBalances);

        List<SimplifiedBalanceResponse.Transaction> response = transactions.stream()
                .map(t -> {
                    String fromName = userRepository.findById(t.getFromUser()).map(u -> u.getName()).orElse("Unknown");
                    String toName = userRepository.findById(t.getToUser()).map(u -> u.getName()).orElse("Unknown");

                    return SimplifiedBalanceResponse.Transaction.builder()
                            .fromUser(t.getFromUser())
                            .fromUserName(fromName)
                            .toUser(t.getToUser())
                            .toUserName(toName)
                            .amount(t.getAmount())
                            .build();
                })
                .collect(Collectors.toList());

        return SimplifiedBalanceResponse.builder()
                .groupId(groupId)
                .transactions(response)
                .build();
    }

    private Map<UUID, BigDecimal> calculateNetBalances(UUID userId) {
        List<ExpenseSplit> splits = splitRepository.findByUserId(userId);
        Map<UUID, BigDecimal> balances = new HashMap<>();

        for (ExpenseSplit split : splits) {
            Expense expense = split.getExpense();
            UUID payerId = expense.getPaidBy().getId();
            UUID ownerId = split.getUser().getId();

            if (!ownerId.equals(payerId) && !split.getPaid()) {
                balances.merge(payerId, split.getAmountOwed().negate(), BigDecimal::add);
            }
        }

        List<Settlement> settlementsFrom = settlementRepository.findByFromUserId(userId);
        for (Settlement settlement : settlementsFrom) {
            balances.merge(settlement.getToUser().getId(), settlement.getAmount(), BigDecimal::add);
        }

        List<Settlement> settlementsTo = settlementRepository.findByToUserId(userId);
        for (Settlement settlement : settlementsTo) {
            balances.merge(settlement.getFromUser().getId(), settlement.getAmount().negate(), BigDecimal::add);
        }

        return balances;
    }

    private Map<UUID, BigDecimal> calculateGroupBalances(UUID groupId) {
        List<ExpenseSplit> splits = splitRepository.findByExpenseGroupId(groupId);
        Map<UUID, BigDecimal> balances = new HashMap<>();

        for (ExpenseSplit split : splits) {
            Expense expense = split.getExpense();
            UUID payerId = expense.getPaidBy().getId();
            UUID ownerId = split.getUser().getId();

            if (payerId.equals(ownerId)) {
                balances.merge(payerId, expense.getTotalAmount().subtract(split.getAmountOwed()), BigDecimal::add);
            } else {
                balances.merge(ownerId, split.getAmountOwed().negate(), BigDecimal::add);
            }
        }

        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        for (Settlement settlement : settlements) {
            balances.merge(settlement.getFromUser().getId(), settlement.getAmount(), BigDecimal::add);
            balances.merge(settlement.getToUser().getId(), settlement.getAmount().negate(), BigDecimal::add);
        }

        return balances;
    }
}