package com.expensesharing.service;

import com.expensesharing.dto.request.CreateSettlementRequest;
import com.expensesharing.entity.Group;
import com.expensesharing.entity.Settlement;
import com.expensesharing.entity.User;
import com.expensesharing.exception.ResourceNotFoundException;
import com.expensesharing.repository.GroupRepository;
import com.expensesharing.repository.SettlementRepository;
import com.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public Settlement createSettlement(CreateSettlementRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User fromUser = userRepository.findById(request.getFromUser())
                .orElseThrow(() -> new ResourceNotFoundException("From user not found"));

        User toUser = userRepository.findById(request.getToUser())
                .orElseThrow(() -> new ResourceNotFoundException("To user not found"));

        Settlement settlement = new Settlement();
        settlement.setGroup(group);
        settlement.setFromUser(fromUser);
        settlement.setToUser(toUser);
        settlement.setAmount(request.getAmount());
        settlement.setNote(request.getNote());

        return settlementRepository.save(settlement);
    }
}