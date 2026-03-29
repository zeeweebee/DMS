package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryDTO {
    private Long enquiryId;

    // Customer summary
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Model summary
    private Long modelId;
    private String modelName;
    private String variant;

    // Dealer summary
    private Long dealerId;
    private String dealerName;

    private String source;
    private LocalDate enquiryDate;
    private String status;
    private LocalDateTime createdAt;
}
