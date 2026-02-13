package com.expensesharing.service;

import com.expensesharing.dto.request.LoginRequest;
import com.expensesharing.dto.request.RegisterRequest;
import com.expensesharing.dto.response.AuthResponse;
import com.expensesharing.entity.Role;  // ← ADD THIS IMPORT
import com.expensesharing.entity.User;
import com.expensesharing.repository.UserRepository;
import com.expensesharing.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);  // ← FIXED: Changed from "USER" to Role.USER

        User savedUser = userRepository.save(user);

        // Generate token - User implements UserDetails, pass it directly
        String token = jwtUtil.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate token - User implements UserDetails
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .message("Login successful")
                .build();
    }
}