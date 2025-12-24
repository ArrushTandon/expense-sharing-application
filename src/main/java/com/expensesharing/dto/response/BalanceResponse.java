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
public class BalanceResponse {
    private List<BalanceDetail> owes;
    private List<BalanceDetail> owedBy;
    private BigDecimal netBalance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceDetail {
        private UUID userId;
        private String userName;
        private BigDecimal amount;
    }
}