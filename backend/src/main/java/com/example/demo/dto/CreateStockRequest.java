package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStockRequest {
    private String vin;
    private Long modelId;
    // dealerId intentionally NOT included — derived from JWT
    private String color;
    private LocalDate manufactureDate;
}