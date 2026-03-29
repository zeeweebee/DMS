package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnquiryRequest {

    // Either provide customerId (existing customer) OR customerName+phone+email (new)
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private Long modelId;
    // dealerId: required for ADMIN; derived from JWT for DEALER
    private Long dealerId;

    private String source;
    private LocalDate enquiryDate;
    private String status; // defaults to NEW
}
