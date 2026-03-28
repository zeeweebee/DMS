package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dealers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Dealer extends BaseEntity {

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
}
