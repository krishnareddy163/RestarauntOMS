package com.example.restaurant.controller;

import com.example.restaurant.monitoring.RestaurantMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final RestaurantMetricsService metricsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("service", "RestaurantOS");
        healthData.put("version", "1.0.0");
        
        return ResponseEntity.ok(healthData);
    }

    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> getReadinessProbe() {
        Map<String, Object> readinessData = new HashMap<>();
        readinessData.put("ready", true);
        readinessData.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(readinessData);
    }

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, Object>> getLivenessProbe() {
        Map<String, Object> livenessData = new HashMap<>();
        livenessData.put("alive", true);
        livenessData.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(livenessData);
    }
}

