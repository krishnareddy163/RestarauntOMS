package com.example.restaurant.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long id;
    private String email;
    private String name;
    private String role;

    public AuthResponse(String token, Long id, String email, String name, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
