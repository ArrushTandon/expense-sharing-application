package com.expensesharing.service;

import com.expensesharing.dto.request.CreateGroupRequest;
import com.expensesharing.dto.response.GroupResponse;
import com.expensesharing.dto.response.UserResponse;
import com.expensesharing.entity.Group;
import com.expensesharing.entity.GroupMember;
import com.expensesharing.entity.User;
import com.expensesharing.exception.ResourceNotFoundException;
import com.expensesharing.repository.GroupMemberRepository;
import com.expensesharing.repository.GroupRepository;
import com.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupResponse createGroup(CreateGroupRequest request) {
        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);

        Group savedGroup = groupRepository.save(group);

        addMemberToGroup(savedGroup, creator);

        if (request.getMemberIds() != null) {
            for (UUID memberId : request.getMemberIds()) {
                if (!memberId.equals(creator.getId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + memberId));
                    addMemberToGroup(savedGroup, member);
                }
            }
        }

        return mapToResponse(savedGroup);
    }

    private void addMemberToGroup(Group group, User user) {
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setIsActive(true);
        groupMemberRepository.save(member);
        group.getMembers().add(member);
    }

    public GroupResponse getGroup(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return mapToResponse(group);
    }

    private GroupResponse mapToResponse(Group group) {
        List<UserResponse> members = groupMemberRepository
                .findByGroupIdAndIsActive(group.getId(), true)
                .stream()
                .map(gm -> UserResponse.builder()
                        .id(gm.getUser().getId())
                        .name(gm.getUser().getName())
                        .email(gm.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getId())
                .createdByName(group.getCreatedBy().getName())
                .createdAt(group.getCreatedAt())
                .members(members)
                .build();
    }

    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

}
