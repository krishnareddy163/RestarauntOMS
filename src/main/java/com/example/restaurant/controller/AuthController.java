package com.example.restaurant.controller;

import com.example.restaurant.dto.*;
import com.example.restaurant.entity.User;
import com.example.restaurant.repository.UserRepository;
import com.example.restaurant.security.UserDetailsImpl;
import com.example.restaurant.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "servlet", matchIfMissing = true)
@ConditionalOnProperty(name = "restaurantos.security.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetailsImpl userDetails) || userDetails.getId() == null) {
                log.error("Authentication principal is missing required user details for {}", loginRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid email or password"));
            }

            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new AuthResponse(jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getName(),
                    userDetails.getRole().name()));
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid email or password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            User user = User.builder()
                    .email(signUpRequest.getEmail())
                    .password(encoder.encode(signUpRequest.getPassword()))
                    .name(signUpRequest.getName())
                    .phone(signUpRequest.getPhone())
                    .role(User.UserRole.valueOf(signUpRequest.getRole().toUpperCase()))
                    .active(true)
                    .build();

            userRepository.save(user);

            log.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            log.error("Registration failed for user: {}", signUpRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Registration failed"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("You've been signed out!"));
    }
}
