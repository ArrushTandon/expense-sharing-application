package com.expensesharing.service;

import com.expensesharing.dto.request.CreateExpenseRequest;
import com.expensesharing.dto.response.ExpenseResponse;
import com.expensesharing.entity.*;
import com.expensesharing.exception.ResourceNotFoundException;
import com.expensesharing.exception.UnauthorizedException;
import com.expensesharing.repository.ExpenseRepository;
import com.expensesharing.repository.GroupMemberRepository;
import com.expensesharing.repository.GroupRepository;
import com.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SplitCalculator splitCalculator;

    public ExpenseResponse createExpense(CreateExpenseRequest request, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is a member of the group
        if (!isMemberOfGroup(request.getGroupId(), requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User payer = userRepository.findById(request.getPaidBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getTotalAmount());
        expense.setPaidBy(payer);
        expense.setSplitType(request.getSplitType());

        Map<UUID, BigDecimal> splits = calculateSplits(request);

        List<ExpenseSplit> expenseSplits = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : splits.entrySet()) {
            User user = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(expense);
            split.setUser(user);
            split.setAmountOwed(entry.getValue());
            split.setPaid(user.getId().equals(payer.getId()));

            if (request.getSplitType() == SplitType.PERCENTAGE) {
                split.setPercentage(request.getSplits().stream()
                        .filter(s -> s.getUserId().equals(entry.getKey()))
                        .findFirst()
                        .map(s -> s.getPercentage())
                        .orElse(null));
            }

            expenseSplits.add(split);
        }

        expense.setSplits(expenseSplits);
        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    private Map<UUID, BigDecimal> calculateSplits(CreateExpenseRequest request) {
        return switch (request.getSplitType()) {
            case EQUAL -> {
                List<UUID> participants = request.getSplits().stream()
                        .map(s -> s.getUserId())
                        .collect(Collectors.toList());
                yield splitCalculator.calculateEqualSplit(request.getTotalAmount(), participants);
            }
            case EXACT -> splitCalculator.calculateExactSplit(request.getTotalAmount(), request.getSplits());
            case PERCENTAGE -> splitCalculator.calculatePercentageSplit(request.getTotalAmount(), request.getSplits());
        };
    }

    public List<ExpenseResponse> getGroupExpenses(UUID groupId, Pageable pageable, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is a member of the group
        if (!isMemberOfGroup(groupId, requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return expenseRepository.findByGroupId(groupId, pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpense(UUID expenseId, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        // Check if user is a member of the group
        if (!isMemberOfGroup(expense.getGroup().getId(), requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return mapToResponse(expense);
    }

    private boolean isMemberOfGroup(UUID groupId, UUID userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(GroupMember::getIsActive)
                .orElse(false);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        List<ExpenseResponse.SplitDetail> splitDetails = expense.getSplits().stream()
                .map(split -> ExpenseResponse.SplitDetail.builder()
                        .userId(split.getUser().getId())
                        .userName(split.getUser().getName())
                        .amountOwed(split.getAmountOwed())
                        .percentage(split.getPercentage())
                        .paid(split.getPaid())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .totalAmount(expense.getTotalAmount())
                .paidBy(expense.getPaidBy().getId())
                .paidByName(expense.getPaidBy().getName())
                .splitType(expense.getSplitType())
                .createdAt(expense.getCreatedAt())
                .splits(splitDetails)
                .build();
    }
}