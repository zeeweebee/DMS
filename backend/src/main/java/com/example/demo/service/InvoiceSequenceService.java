package com.example.demo.service;

import com.example.demo.entity.InvoiceCounter;
import com.example.demo.repository.InvoiceCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Generates unique, sequential invoice numbers of the form:
 *   INV-{YYYY}-{000001}
 *
 * Uses a PESSIMISTIC_WRITE lock on the invoice_counter row for the
 * current year so concurrent requests never produce the same number.
 *
 * Runs in its own REQUIRES_NEW transaction so the lock is released
 * as soon as the number is reserved, regardless of what the calling
 * transaction does next.
 */
@Service
@RequiredArgsConstructor
public class InvoiceSequenceService {

    private final InvoiceCounterRepository counterRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextInvoiceNumber() {
        int year = LocalDate.now().getYear();

        InvoiceCounter counter = counterRepository
                .findByYearForUpdate(year)
                .orElseGet(() -> InvoiceCounter.builder()
                        .year(year)
                        .lastSequence(0L)
                        .build());

        long next = counter.getLastSequence() + 1;
        counter.setLastSequence(next);
        counterRepository.save(counter);

        // INV-2025-000001
        return String.format("INV-%d-%06d", year, next);
    }
}
