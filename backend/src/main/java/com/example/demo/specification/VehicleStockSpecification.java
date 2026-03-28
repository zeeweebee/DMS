package com.example.demo.specification;

import com.example.demo.entity.VehicleStock;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VehicleStockSpecification {

    private VehicleStockSpecification() {}

    /**
     * Builds a composite specification for stock queries.
     *
     * @param keyword     VIN search (partial, case-insensitive)
     * @param stockStatus filter by AVAILABLE / BOOKED / SOLD
     * @param dealerId    filter by dealer (null = no filter, used for ADMIN; always set for DEALER role)
     * @param modelId     filter by model
     */
    public static Specification<VehicleStock> build(String keyword, String stockStatus,
                                                     Long dealerId, Long modelId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search on VIN
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("vin")), pattern));
            }

            // Filter: stockStatus
            if (stockStatus != null && !stockStatus.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("stockStatus")), stockStatus.toUpperCase()));
            }

            // Filter: dealerId (always applied for DEALER role)
            if (dealerId != null) {
                predicates.add(cb.equal(root.get("dealer").get("dealerId"), dealerId));
            }

            // Filter: modelId
            if (modelId != null) {
                predicates.add(cb.equal(root.get("model").get("modelId"), modelId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
