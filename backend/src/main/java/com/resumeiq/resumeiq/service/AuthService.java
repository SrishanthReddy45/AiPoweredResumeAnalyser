package com.resumeiq.resumeiq.service;

import com.resumeiq.resumeiq.dto.AuthDtos;
import com.resumeiq.resumeiq.entity.Role;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.exception.BadRequestException;
import com.resumeiq.resumeiq.exception.ConflictException;
import com.resumeiq.resumeiq.repository.UserRepository;
import com.resumeiq.resumeiq.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new ConflictException("A user with this email already exists");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthDtos.AuthResponse(token, toUserDto(user));
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found"));
        String token = jwtService.generateToken(user);
        return new AuthDtos.AuthResponse(token, toUserDto(user));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    public AuthDtos.UserDto toUserDto(User user) {
        return new AuthDtos.UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
