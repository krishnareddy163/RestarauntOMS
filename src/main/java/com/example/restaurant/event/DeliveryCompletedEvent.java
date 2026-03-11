package com.example.restaurant.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCompletedEvent {
    @JsonProperty("delivery_id")
    private Long deliveryId;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("driver_id")
    private Long driverId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("delivered_at")
    private String deliveredAt;
}

