package com.expensesharing.controller;

import com.expensesharing.dto.request.CreateExpenseRequest;
import com.expensesharing.dto.response.ExpenseResponse;
import com.expensesharing.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request,
            Authentication authentication) {
        request.setGroupId(groupId);
        ExpenseResponse response = expenseService.createExpense(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        List<ExpenseResponse> expenses = expenseService.getGroupExpenses(
                groupId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()),
                authentication
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            Authentication authentication) {
        ExpenseResponse expense = expenseService.getExpense(expenseId, authentication);
        return ResponseEntity.ok(expense);
    }
}