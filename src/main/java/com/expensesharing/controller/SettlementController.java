package com.expensesharing.controller;

import com.expensesharing.dto.request.CreateSettlementRequest;
import com.expensesharing.entity.Settlement;
import com.expensesharing.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<Settlement> createSettlement(@Valid @RequestBody CreateSettlementRequest request) {
        Settlement settlement = settlementService.createSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlement);
    }
}