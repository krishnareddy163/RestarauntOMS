package com.example.restaurant.security.services;

import com.example.restaurant.entity.User;
import com.example.restaurant.repository.UserRepository;
import com.example.restaurant.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_success() {
        User user = User.builder().id(1L).email("user@test.com").role(User.UserRole.CUSTOMER).active(true).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        var details = userDetailsService.loadUserByUsername("user@test.com");

        assertTrue(details instanceof UserDetailsImpl);
        assertEquals("user@test.com", details.getUsername());
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("missing@test.com"));
    }
}
