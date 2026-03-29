package com.example.demo.specification;

import com.example.demo.entity.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    private BookingSpecification() {}

    public static Specification<Booking> build(String status, Long dealerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("bookingStatus")), status.toUpperCase()));
            }
            if (dealerId != null) {
                predicates.add(cb.equal(root.get("dealer").get("dealerId"), dealerId));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
