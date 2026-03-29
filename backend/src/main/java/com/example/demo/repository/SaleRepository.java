package com.example.demo.repository;

import com.example.demo.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long>,
        JpaSpecificationExecutor<Sale> {

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    boolean existsByBookingBookingId(Long bookingId);

    Optional<Sale> findByBookingBookingId(Long bookingId);
}
