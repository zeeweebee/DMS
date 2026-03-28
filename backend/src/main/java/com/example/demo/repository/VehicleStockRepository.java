package com.example.demo.repository;

import com.example.demo.entity.VehicleStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VehicleStockRepository extends JpaRepository<VehicleStock, String>, JpaSpecificationExecutor<VehicleStock> {

    List<VehicleStock> findByDealerDealerId(Long dealerId);

    List<VehicleStock> findByDealerDealerIdAndStockStatus(Long dealerId, String stockStatus);

    Optional<VehicleStock> findByVinAndDealerDealerId(String vin, Long dealerId);

    boolean existsByVin(String vin);
}
