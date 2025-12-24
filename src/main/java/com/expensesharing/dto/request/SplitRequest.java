package com.expensesharing.dto.request;

import jakarta.validation.constraints.DecimalMax;
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
public class SplitRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    private BigDecimal amount;

    @DecimalMin(value = "0.01")
    @DecimalMax(value = "100.00")
    private BigDecimal percentage;
}