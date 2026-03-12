package com.example.restaurant.security.jwt;

import com.example.restaurant.security.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Base64 for 64 bytes key
        jwtSecret = "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUYwMTIzNDU2Nzg5QUJDREVG";
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    }

    @Test
    void generateJwtToken_success() {
        UserDetailsImpl principal = new UserDetailsImpl(1L, "user@test.com", "pw", "User", "123",
                com.example.restaurant.entity.User.UserRole.CUSTOMER, true);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String token = jwtUtils.generateJwtToken(auth);

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("user@test.com", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void generateJwtToken_nullPrincipal_throws() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null);
        assertThrows(IllegalArgumentException.class, () -> jwtUtils.generateJwtToken(auth));
    }

    @Test
    void generateJwtToken_nullAuthentication_throws() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtils.generateJwtToken(null));
    }

    @Test
    void generateJwtToken_nullUsername_throws() {
        UserDetailsImpl principal = new UserDetailsImpl(1L, null, "pw", "User", "123",
                com.example.restaurant.entity.User.UserRole.CUSTOMER, true);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        assertThrows(IllegalArgumentException.class, () -> jwtUtils.generateJwtToken(auth));
    }

    @Test
    void validateJwtToken_invalid_returnsFalse() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.value"));
    }

    @Test
    void validateJwtToken_expired_returnsFalse() {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        String token = Jwts.builder()
                .setSubject("user@test.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
                .setExpiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_empty_returnsFalse() {
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    void validateJwtToken_unsupported_returnsFalse() {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        String token = Jwts.builder()
                .setHeaderParam("zip", "FOO")
                .setSubject("user@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void getUserNameFromJwtToken_success() {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        String token = Jwts.builder()
                .setSubject("another@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertEquals("another@test.com", jwtUtils.getUserNameFromJwtToken(token));
    }
}
