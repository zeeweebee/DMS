package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDTO {
    private Long saleId;
    private Long bookingId;

    // Invoice
    private String invoiceNumber;
    private LocalDate saleDate;
    private String paymentStatus;

    // Customer
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Vehicle
    private String vin;
    private String modelName;
    private String variant;
    private String fuelType;
    private String transmission;
    private String color;
    private String manufactureDate;

    // Dealer
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;
    private String dealerPhone;
    private String dealerEmail;

    // Financials
    private BigDecimal exShowroomPrice;
    private BigDecimal bookingAmount;   // advance already paid
    private BigDecimal salePrice;       // final agreed price
    private String paymentMode;
    private BigDecimal loanAmount;
    private String financeBank;
    private String exchangeVehicle;
    private BigDecimal exchangeValue;
    private String remarks;

    private LocalDateTime createdAt;
}
