package com.example.demo.repository;

import com.example.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {

    List<Booking> findByDealerDealerIdOrderByCreatedAtDesc(Long dealerId);

    Optional<Booking> findByVehicleStockVinAndBookingStatusNot(String vin, String status);

    boolean existsByVehicleStockVinAndBookingStatusNot(String vin, String cancelledStatus);
}
