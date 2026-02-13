package com.expensesharing.service;

import com.expensesharing.dto.request.CreateUserRequest;
import com.expensesharing.dto.response.UserResponse;
import com.expensesharing.entity.Role;
import com.expensesharing.entity.User;
import com.expensesharing.exception.ResourceNotFoundException;
import com.expensesharing.exception.UnauthorizedException;
import com.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public UserResponse getUser(UUID userId, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only allow users to see their own details OR admins to see anyone's details
        if (!requestingUser.getId().equals(userId) && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You can only view your own profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        // This method is now protected by @PreAuthorize("hasRole('ADMIN')") in controller
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}