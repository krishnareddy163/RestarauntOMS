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
public class PreparationCompletedEvent {
    @JsonProperty("preparation_id")
    private Long preparationId;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("completed_at")
    private String completedAt;
}

