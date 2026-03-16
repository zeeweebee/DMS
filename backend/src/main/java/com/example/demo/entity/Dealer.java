package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dealers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealerId;

    private String dealerName;

    @Column(unique = true)
    private String dealerCode;

    private String address;

    private Long cityId;

    private Long stateId;

    private String phone;

    private String email;

    private String status;

    private LocalDateTime createdAt;
}