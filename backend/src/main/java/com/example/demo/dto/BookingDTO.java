package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long bookingId;

    // Enquiry / Customer summary
    private Long enquiryId;
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Vehicle summary
    private String vin;
    private String modelName;
    private String variant;
    private String color;

    // Dealer summary
    private Long dealerId;
    private String dealerName;

    private LocalDate bookingDate;
    private BigDecimal bookingAmount;
    private String bookingStatus;

    private String cancellationReason;
    private BigDecimal refundAmount;

    private LocalDateTime createdAt;
}
