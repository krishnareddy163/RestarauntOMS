package com.example.restaurant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RestaurantOSApplicationTest {

    @Test
    void main_startsWithNoWeb() {
        assertDoesNotThrow(() ->
                RestaurantOSApplication.main(new String[]{"--spring.main.web-application-type=none"})
        );
    }
}
