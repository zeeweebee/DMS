package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModelDTO {
    private Long modelId;
    private String modelName;
    private String variant;
    private String fuelType;
    private String transmission;
    private BigDecimal exShowroomPrice;
    private String status;
}