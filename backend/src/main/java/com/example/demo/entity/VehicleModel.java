package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "models")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @Column(nullable = false)
    private String modelName;

    private String variant;

    @Column(nullable = false)
    private String fuelType;       // PETROL, DIESEL, EV

    @Column(nullable = false)
    private String transmission;   // MANUAL, AUTOMATIC

    @Column(nullable = false)
    private BigDecimal exShowroomPrice;

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
}