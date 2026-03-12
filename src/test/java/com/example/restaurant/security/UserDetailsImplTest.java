package com.example.restaurant.security;

import com.example.restaurant.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void buildAndAuthorities() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("pw")
                .name("User")
                .phone("123")
                .role(User.UserRole.ADMIN)
                .active(true)
                .build();

        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals("user@test.com", details.getUsername());
        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.isEnabled());
    }

    @Test
    void equalsAndHashCode() {
        UserDetailsImpl a = new UserDetailsImpl(1L, "a@test.com", "pw", "A", "1", User.UserRole.CUSTOMER, true);
        UserDetailsImpl b = new UserDetailsImpl(1L, "b@test.com", "pw", "B", "2", User.UserRole.CUSTOMER, true);
        UserDetailsImpl c = new UserDetailsImpl(2L, "c@test.com", "pw", "C", "3", User.UserRole.CUSTOMER, true);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsHandlesNullAndDifferentClass() {
        UserDetailsImpl a = new UserDetailsImpl(1L, "a@test.com", "pw", "A", "1", User.UserRole.CUSTOMER, true);

        assertNotEquals(a, null);
        assertNotEquals(a, "not-a-user");
        assertEquals(a, a);
    }

    @Test
    void accountFlagsDefaultToTrue() {
        UserDetailsImpl details = new UserDetailsImpl(1L, "a@test.com", "pw", "A", "1", User.UserRole.CUSTOMER, true);

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
    }

    @Test
    void isEnabledReflectsActiveFlag() {
        UserDetailsImpl inactive = new UserDetailsImpl(1L, "a@test.com", "pw", "A", "1", User.UserRole.CUSTOMER, false);

        assertFalse(inactive.isEnabled());
    }
}
