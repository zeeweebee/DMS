package com.example.demo.specification;

import com.example.demo.entity.Dealer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DealerSpecification {

    private DealerSpecification() {}

    /**
     * Keyword search on dealerName or dealerCode (case-insensitive).
     */
    public static Specification<Dealer> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("dealerName")), pattern),
                cb.like(cb.lower(root.get("dealerCode")), pattern)
            );
        };
    }

    /**
     * Filter by status.
     */
    public static Specification<Dealer> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get("status")), status.toUpperCase());
        };
    }

    public static Specification<Dealer> build(String keyword, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("dealerName")), pattern),
                    cb.like(cb.lower(root.get("dealerCode")), pattern)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
