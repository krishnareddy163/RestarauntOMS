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
public class PaymentProcessedEvent {
    @JsonProperty("payment_id")
    private Long paymentId;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("processed_at")
    private String processedAt;
}

