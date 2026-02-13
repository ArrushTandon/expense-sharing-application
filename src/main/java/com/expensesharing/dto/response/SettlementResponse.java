package com.expensesharing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID fromUserId;
    private String fromUserName;
    private UUID toUserId;
    private String toUserName;
    private BigDecimal amount;
    private LocalDateTime settledAt;
    private String note;
}
