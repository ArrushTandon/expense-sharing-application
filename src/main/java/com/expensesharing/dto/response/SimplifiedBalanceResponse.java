package com.expensesharing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedBalanceResponse {
    private UUID groupId;
    private List<Transaction> transactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
        private UUID fromUser;
        private String fromUserName;
        private UUID toUser;
        private String toUserName;
        private BigDecimal amount;
    }
}