package com.example.restaurant.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String role;
}
