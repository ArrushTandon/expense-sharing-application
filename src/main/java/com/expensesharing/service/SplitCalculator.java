package com.expensesharing.service;

import com.expensesharing.dto.request.SplitRequest;
import com.expensesharing.exception.InvalidSplitException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class SplitCalculator {

    public Map<UUID, BigDecimal> calculateEqualSplit(BigDecimal totalAmount, List<UUID> participants) {
        if (participants.isEmpty()) {
            throw new InvalidSplitException("Participants list cannot be empty");
        }

        int participantCount = participants.size();
        BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);

        Map<UUID, BigDecimal> splits = new HashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;

        for (int i = 0; i < participants.size(); i++) {
            UUID userId = participants.get(i);

            if (i == participants.size() - 1) {
                splits.put(userId, totalAmount.subtract(allocated));
            } else {
                splits.put(userId, perPerson);
                allocated = allocated.add(perPerson);
            }
        }

        return splits;
    }

    public Map<UUID, BigDecimal> calculateExactSplit(BigDecimal totalAmount, List<SplitRequest> splits) {
        BigDecimal sum = splits.stream()
                .map(SplitRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(totalAmount) != 0) {
            throw new InvalidSplitException(
                    String.format("Sum of splits (%.2f) must equal total amount (%.2f)", sum, totalAmount)
            );
        }

        Map<UUID, BigDecimal> result = new HashMap<>();
        for (SplitRequest split : splits) {
            if (split.getAmount() == null) {
                throw new InvalidSplitException("Amount is required for exact split type");
            }
            result.put(split.getUserId(), split.getAmount());
        }

        return result;
    }

    public Map<UUID, BigDecimal> calculatePercentageSplit(BigDecimal totalAmount, List<SplitRequest> splits) {
        BigDecimal totalPercentage = splits.stream()
                .map(SplitRequest::getPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvalidSplitException(
                    String.format("Percentages must sum to 100, got %.2f", totalPercentage)
            );
        }

        Map<UUID, BigDecimal> result = new HashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;

        for (int i = 0; i < splits.size(); i++) {
            SplitRequest split = splits.get(i);
            if (split.getPercentage() == null) {
                throw new InvalidSplitException("Percentage is required for percentage split type");
            }

            BigDecimal amount;
            if (i == splits.size() - 1) {
                amount = totalAmount.subtract(allocated);
            } else {
                amount = totalAmount
                        .multiply(split.getPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                allocated = allocated.add(amount);
            }

            result.put(split.getUserId(), amount);
        }

        return result;
    }
}
