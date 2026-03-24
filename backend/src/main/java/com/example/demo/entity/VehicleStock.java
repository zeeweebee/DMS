package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleStock {

    @Id
    @Column(length = 50)
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    private String color;

    private LocalDate manufactureDate;

    @Builder.Default
    private String stockStatus = "AVAILABLE"; // AVAILABLE, BOOKED, SOLD

    @CreationTimestamp
    private LocalDateTime createdAt;
}