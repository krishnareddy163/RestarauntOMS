package com.example.restaurant.controller;

import com.example.restaurant.monitoring.RestaurantMetricsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HealthCheckControllerTest {

    @Mock
    private RestaurantMetricsService metricsService;

    @InjectMocks
    private HealthCheckController healthCheckController;

    @Test
    void getHealth_returnsStatus() {
        ResponseEntity<Map<String, Object>> res = healthCheckController.getHealth();
        assertEquals(200, res.getStatusCode().value());
        assertEquals("UP", res.getBody().get("status"));
    }

    @Test
    void getReadiness_returnsReady() {
        ResponseEntity<Map<String, Object>> res = healthCheckController.getReadinessProbe();
        assertEquals(200, res.getStatusCode().value());
        assertTrue((Boolean) res.getBody().get("ready"));
    }

    @Test
    void getLiveness_returnsAlive() {
        ResponseEntity<Map<String, Object>> res = healthCheckController.getLivenessProbe();
        assertEquals(200, res.getStatusCode().value());
        assertTrue((Boolean) res.getBody().get("alive"));
    }
}
