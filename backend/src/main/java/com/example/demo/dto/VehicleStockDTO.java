package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleStockDTO {
    private String vin;
    private Long modelId;
    private String modelName;
    private String variant;
    private Long dealerId;
    private String dealerName;
    private String color;
    private LocalDate manufactureDate;
    private String stockStatus;
    private LocalDateTime createdAt;
}