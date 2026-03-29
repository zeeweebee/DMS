package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Holds the last-used invoice sequence number for each calendar year.
 * One row per year, updated atomically via a SELECT … FOR UPDATE lock
 * in InvoiceSequenceService to avoid duplicate invoice numbers under load.
 *
 * Example row: year=2025, lastSequence=42
 * → next invoice will be INV-2025-000043
 */
@Entity
@Table(name = "invoice_counter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCounter {

    @Id
    @Column(name = "year")
    private Integer year;

    @Column(name = "last_sequence", nullable = false)
    private Long lastSequence;
}
