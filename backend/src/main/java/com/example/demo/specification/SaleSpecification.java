package com.example.demo.specification;

import com.example.demo.entity.Sale;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SaleSpecification {

    private SaleSpecification() {}

    /**
     * @param keyword       searches invoiceNumber, customerName, VIN
     * @param paymentStatus PENDING | PAID
     * @param dealerId      scopes to a specific dealer (forced for DEALER role)
     */
    public static Specification<Sale> build(String keyword, String paymentStatus, Long dealerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String p = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("invoiceNumber")), p),
                    cb.like(cb.lower(root.get("booking").get("enquiry")
                            .get("customer").get("customerName")), p),
                    cb.like(cb.lower(root.get("vehicleStock").get("vin")), p)
                ));
            }

            if (paymentStatus != null && !paymentStatus.isBlank()) {
                predicates.add(cb.equal(
                    cb.upper(root.get("paymentStatus")), paymentStatus.toUpperCase()));
            }

            if (dealerId != null) {
                predicates.add(cb.equal(
                    root.get("booking").get("dealer").get("dealerId"), dealerId));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
