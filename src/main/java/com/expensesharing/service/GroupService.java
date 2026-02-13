package com.expensesharing.service;

import com.expensesharing.dto.request.AddMemberRequest;
import com.expensesharing.dto.request.CreateGroupRequest;
import com.expensesharing.dto.response.GroupResponse;
import com.expensesharing.dto.response.UserResponse;
import com.expensesharing.entity.Group;
import com.expensesharing.entity.GroupMember;
import com.expensesharing.entity.Role;
import com.expensesharing.entity.User;
import com.expensesharing.exception.ResourceNotFoundException;
import com.expensesharing.exception.UnauthorizedException;
import com.expensesharing.repository.GroupMemberRepository;
import com.expensesharing.repository.GroupRepository;
import com.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getCreatedBy()));

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);

        Group savedGroup = groupRepository.save(group);

        addMemberToGroupInternal(savedGroup, creator);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (UUID memberId : request.getMemberIds()) {
                if (!memberId.equals(creator.getId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + memberId));
                    addMemberToGroupInternal(savedGroup, member);
                }
            }
        }

        return getGroupResponse(savedGroup.getId());
    }

    private void addMemberToGroupInternal(Group group, User user) {
        if (groupMemberRepository.findByGroupIdAndUserId(group.getId(), user.getId()).isEmpty()) {
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setIsActive(true);
            groupMemberRepository.save(member);
        }
    }

    public GroupResponse getGroup(UUID groupId, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is a member of the group OR is an admin
        if (!isMemberOfGroup(groupId, requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        return getGroupResponse(groupId);
    }

    public GroupResponse addMemberToGroup(UUID groupId, AddMemberRequest request, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only members can add other members
        if (!isMemberOfGroup(groupId, requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (groupMemberRepository.findByGroupIdAndUserId(groupId, request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setIsActive(true);
        groupMemberRepository.save(member);

        return getGroupResponse(groupId);
    }

    public GroupResponse removeMemberFromGroup(UUID groupId, UUID userId, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only members can remove other members
        if (!isMemberOfGroup(groupId, requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

        if (group.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove the group creator");
        }

        member.setIsActive(false);
        groupMemberRepository.save(member);

        return getGroupResponse(groupId);
    }

    public List<GroupResponse> getUserGroups(Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If admin, return all groups
        if (requestingUser.getRole() == Role.ADMIN) {
            return groupRepository.findAll()
                    .stream()
                    .map(group -> getGroupResponse(group.getId()))
                    .collect(Collectors.toList());
        }

        // Otherwise, return only groups the user is a member of
        return groupMemberRepository.findActiveByUserId(requestingUser.getId())
                .stream()
                .map(gm -> getGroupResponse(gm.getGroup().getId()))
                .collect(Collectors.toList());
    }

    private boolean isMemberOfGroup(UUID groupId, UUID userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(GroupMember::getIsActive)
                .orElse(false);
    }

    private GroupResponse getGroupResponse(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<UserResponse> members = groupMemberRepository
                .findByGroupIdAndIsActive(group.getId(), true)
                .stream()
                .map(gm -> {
                    User user = gm.getUser();
                    return UserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .createdAt(user.getCreatedAt())
                            .build();
                })
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
}