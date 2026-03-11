package com.example.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the RestaurantOS application.
 * 
 * RestaurantOS - A comprehensive, scalable restaurant management platform
 * 
 * Features:
 * - Order management (create, track, update)
 * - Payment processing with multiple payment methods
 * - Real-time inventory management
 * - Kitchen preparation tracking
 * - Driver assignment and delivery tracking
 * - Kafka-based event-driven architecture
 * - Prometheus metrics and Grafana dashboards
 * - High concurrency support (10,000+ connections)
 * 
 * @author RestaurantOS Team
 * @version 1.0.0
 */
@SpringBootApplication
public class RestaurantOSApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantOSApplication.class, args);
    }
}

