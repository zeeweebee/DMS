package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "enquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Enquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enquiry_id")
    private Long enquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    /** Walk-in, Phone, Website, Referral, etc. */
    @Column(name = "source")
    private String source;

    @Column(name = "enquiry_date")
    private LocalDate enquiryDate;

    /**
     * Lifecycle: NEW → CONTACTED → TEST_DRIVE → NEGOTIATING → CONVERTED | LOST
     */
    @Builder.Default
    @Column(name = "status")
    private String status = "NEW";
}
