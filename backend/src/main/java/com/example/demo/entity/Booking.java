package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vin", nullable = false)
    private VehicleStock vehicleStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "booking_amount", precision = 12, scale = 2)
    private BigDecimal bookingAmount;

    /**
     * PENDING → CONFIRMED → CANCELLED
     */
    @Builder.Default
    @Column(name = "booking_status")
    private String bookingStatus = "PENDING";

    /** Cancellation reason — populated when bookingStatus = CANCELLED */
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    /** Amount refunded on cancellation */
    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;
}
