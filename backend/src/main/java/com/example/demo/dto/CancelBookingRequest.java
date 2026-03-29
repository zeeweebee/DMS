package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelBookingRequest {
    private String cancellationReason;
    private BigDecimal refundAmount;
}
