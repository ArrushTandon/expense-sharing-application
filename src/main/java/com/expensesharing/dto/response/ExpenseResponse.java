package com.expensesharing.dto.response;

import com.expensesharing.entity.SplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private UUID id;
    private UUID groupId;
    private String description;
    private BigDecimal totalAmount;
    private UUID paidBy;
    private String paidByName;
    private SplitType splitType;
    private LocalDateTime createdAt;
    private List<SplitDetail> splits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDetail {
        private UUID userId;
        private String userName;
        private BigDecimal amountOwed;
        private BigDecimal percentage;
        private Boolean paid;
    }
}