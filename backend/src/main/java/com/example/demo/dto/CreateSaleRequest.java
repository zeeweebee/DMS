package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSaleRequest {

    /** The confirmed (non-cancelled) booking being converted to a sale. */
    private Long bookingId;

    /** Final agreed sale price. */
    private BigDecimal salePrice;

    /** CASH | LOAN | EXCHANGE | MIXED */
    private String paymentMode;

    /** Date of sale/invoice. Defaults to today if null. */
    private LocalDate saleDate;

    // Optional payment detail fields
    private BigDecimal loanAmount;
    private String financeBank;
    private String exchangeVehicle;
    private BigDecimal exchangeValue;
    private String remarks;
}
