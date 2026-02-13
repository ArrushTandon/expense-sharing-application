package com.expensesharing.controller;

import com.expensesharing.dto.request.AddMemberRequest;
import com.expensesharing.dto.request.CreateGroupRequest;
import com.expensesharing.dto.response.GroupResponse;
import com.expensesharing.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable UUID groupId,
            Authentication authentication) {
        GroupResponse response = groupService.getGroup(groupId, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {
        GroupResponse response = groupService.addMemberToGroup(groupId, request, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            Authentication authentication) {
        GroupResponse response = groupService.removeMemberFromGroup(groupId, userId, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(Authentication authentication) {
        // Returns only groups the user is a member of
        return ResponseEntity.ok(groupService.getUserGroups(authentication));
    }
}