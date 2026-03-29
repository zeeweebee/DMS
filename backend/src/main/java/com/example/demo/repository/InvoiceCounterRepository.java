package com.example.demo.repository;

import com.example.demo.entity.InvoiceCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceCounterRepository extends JpaRepository<InvoiceCounter, Integer> {

    /**
     * Locks the row for the given year so no two concurrent transactions
     * can generate the same sequence number.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ic FROM InvoiceCounter ic WHERE ic.year = :year")
    Optional<InvoiceCounter> findByYearForUpdate(@Param("year") Integer year);
}
