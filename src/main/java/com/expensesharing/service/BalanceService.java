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
                // This user owes money to someone
                owes.add(BalanceResponse.BalanceDetail.builder()
                        .userId(entry.getKey())
                        .userName(userName)
                        .amount(amount.abs())
                        .build());
                totalOwing = totalOwing.add(amount.abs());
            } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
                // Someone owes this user money
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

    /**
     * Calculate net balances for a specific user across all their groups
     * Positive value = Others owe this user
     * Negative value = This user owes others
     */
    private Map<UUID, BigDecimal> calculateNetBalances(UUID userId) {
        Map<UUID, BigDecimal> balances = new HashMap<>();

        // Get all expense splits where this user is involved
        List<ExpenseSplit> allSplits = splitRepository.findByUserId(userId);

        for (ExpenseSplit split : allSplits) {
            Expense expense = split.getExpense();
            UUID payerId = expense.getPaidBy().getId();
            UUID splitUserId = split.getUser().getId();

            // If this user is the payer
            if (payerId.equals(userId)) {
                // For each other person's split in this expense, they owe this user
                List<ExpenseSplit> allSplitsInExpense = splitRepository.findByExpenseId(expense.getId());
                for (ExpenseSplit otherSplit : allSplitsInExpense) {
                    UUID otherUserId = otherSplit.getUser().getId();
                    if (!otherUserId.equals(userId) && !otherSplit.getPaid()) {
                        // Other user owes this user (positive balance)
                        balances.merge(otherUserId, otherSplit.getAmountOwed(), BigDecimal::add);
                    }
                }
            }
            // If this user owes money (not the payer and not paid)
            else if (splitUserId.equals(userId) && !split.getPaid()) {
                // This user owes the payer (negative balance)
                balances.merge(payerId, split.getAmountOwed().negate(), BigDecimal::add);
            }
        }

        // Apply settlements
        List<Settlement> settlementsFrom = settlementRepository.findByFromUserId(userId);
        for (Settlement settlement : settlementsFrom) {
            // This user paid toUser, so reduce what this user owes (or toUser owes this user back)
            balances.merge(settlement.getToUser().getId(), settlement.getAmount().negate(), BigDecimal::add);
        }

        List<Settlement> settlementsTo = settlementRepository.findByToUserId(userId);
        for (Settlement settlement : settlementsTo) {
            // fromUser paid this user, so reduce what fromUser owes this user
            balances.merge(settlement.getFromUser().getId(), settlement.getAmount(), BigDecimal::add);
        }

        return balances;
    }

    /**
     * FIXED: Calculate group balances correctly
     * Logic: For each expense split, track who paid what vs who owes what
     */
    private Map<UUID, BigDecimal> calculateGroupBalances(UUID groupId) {
        List<ExpenseSplit> splits = splitRepository.findByExpenseGroupId(groupId);
        Map<UUID, BigDecimal> balances = new HashMap<>();

        // Group splits by expense
        Map<UUID, List<ExpenseSplit>> splitsByExpense = splits.stream()
                .collect(Collectors.groupingBy(split -> split.getExpense().getId()));

        // For each expense
        for (Map.Entry<UUID, List<ExpenseSplit>> entry : splitsByExpense.entrySet()) {
            List<ExpenseSplit> expenseSplits = entry.getValue();
            if (expenseSplits.isEmpty()) continue;

            Expense expense = expenseSplits.get(0).getExpense();
            UUID payerId = expense.getPaidBy().getId();

            // For each split in this expense
            for (ExpenseSplit split : expenseSplits) {
                UUID userId = split.getUser().getId();
                BigDecimal amountOwed = split.getAmountOwed();

                if (userId.equals(payerId)) {
                    // This person paid for the expense
                    // They are owed (totalAmount - their share)
                    BigDecimal theirShare = amountOwed;
                    BigDecimal theyPaid = expense.getTotalAmount();
                    BigDecimal netForPayer = theyPaid.subtract(theirShare);
                    balances.merge(payerId, netForPayer, BigDecimal::add);
                } else {
                    // This person didn't pay
                    // They owe their share to the payer (negative)
                    balances.merge(userId, amountOwed.negate(), BigDecimal::add);
                }
            }
        }

        // Apply settlements
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        for (Settlement settlement : settlements) {
            // fromUser paid toUser
            balances.merge(settlement.getFromUser().getId(), settlement.getAmount(), BigDecimal::add);
            balances.merge(settlement.getToUser().getId(), settlement.getAmount().negate(), BigDecimal::add);
        }

        return balances;
    }
}