package com.example.restaurant.controller;

import com.example.restaurant.dto.AuthResponse;
import com.example.restaurant.dto.LoginRequest;
import com.example.restaurant.dto.MessageResponse;
import com.example.restaurant.dto.SignupRequest;
import com.example.restaurant.entity.User;
import com.example.restaurant.repository.UserRepository;
import com.example.restaurant.security.UserDetailsImpl;
import com.example.restaurant.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("password");

        signupRequest = new SignupRequest();
        signupRequest.setEmail("new@test.com");
        signupRequest.setPassword("password");
        signupRequest.setName("New User");
        signupRequest.setPhone("1234567890");
        signupRequest.setRole("customer");
    }

    @Test
    void authenticateUser_success_returnsAuthResponse() {
        UserDetailsImpl principal = new UserDetailsImpl(1L, "user@test.com", "pw", "User", "123", User.UserRole.CUSTOMER, true);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(AuthResponse.class, response.getBody());
        AuthResponse body = (AuthResponse) response.getBody();
        assertEquals("jwt-token", body.getToken());
        assertEquals("user@test.com", body.getEmail());
    }

    @Test
    void authenticateUser_failure_returnsMessage() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(400, response.getStatusCode().value());
        assertInstanceOf(MessageResponse.class, response.getBody());
        MessageResponse body = (MessageResponse) response.getBody();
        assertEquals("Invalid email or password", body.getMessage());
    }

    @Test
    void registerUser_success_createsUser() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signupRequest.getPassword())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(MessageResponse.class, response.getBody());
        MessageResponse body = (MessageResponse) response.getBody();
        assertEquals("User registered successfully!", body.getMessage());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void registerUser_duplicateEmail_returnsError() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(400, response.getStatusCode().value());
        MessageResponse body = (MessageResponse) response.getBody();
        assertNotNull(body);
        assertEquals("Error: Email is already in use!", body.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_exception_returnsError() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(400, response.getStatusCode().value());
        MessageResponse body = (MessageResponse) response.getBody();
        assertNotNull(body);
        assertEquals("Error: Registration failed", body.getMessage());
    }

    @Test
    void logoutUser_returnsSuccess() {
        ResponseEntity<?> response = authController.logoutUser();
        assertEquals(200, response.getStatusCode().value());
        MessageResponse body = (MessageResponse) response.getBody();
        assertNotNull(body);
        assertEquals("You've been signed out!", body.getMessage());
    }
}
