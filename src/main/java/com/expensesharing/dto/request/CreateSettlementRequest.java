package com.expensesharing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {
    @NotNull(message = "Group ID is required")
    private UUID groupId;

    @NotNull(message = "From user is required")
    private UUID fromUser;

    @NotNull(message = "To user is required")
    private UUID toUser;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String note;
}