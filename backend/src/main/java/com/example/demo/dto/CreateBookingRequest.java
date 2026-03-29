package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    private Long enquiryId;
    /** Optional: if omitted, FIFO (oldest AVAILABLE VIN for the enquiry's model) is used */
    private String vin;
    private LocalDate bookingDate;
    private BigDecimal bookingAmount;
}
