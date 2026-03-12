package com.example.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TestSecurityConfig {
    @Bean
    @Order(0)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
