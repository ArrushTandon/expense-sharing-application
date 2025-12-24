package com.expensesharing.controller;

import com.expensesharing.dto.response.BalanceResponse;
import com.expensesharing.dto.response.SimplifiedBalanceResponse;
import com.expensesharing.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/users/{userId}/balances")
    public ResponseEntity<BalanceResponse> getUserBalances(@PathVariable UUID userId) {
        BalanceResponse balances = balanceService.getUserBalances(userId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/groups/{groupId}/balances")
    public ResponseEntity<SimplifiedBalanceResponse> getGroupBalances(@PathVariable UUID groupId) {
        SimplifiedBalanceResponse balances = balanceService.getSimplifiedGroupBalances(groupId);
        return ResponseEntity.ok(balances);
    }
}