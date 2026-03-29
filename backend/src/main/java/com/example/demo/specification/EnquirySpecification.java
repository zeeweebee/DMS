package com.example.demo.specification;

import com.example.demo.entity.Enquiry;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EnquirySpecification {

    private EnquirySpecification() {}

    public static Specification<Enquiry> build(String keyword, String status, Long dealerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("customer").get("customerName")), pattern),
                    cb.like(cb.lower(root.get("customer").get("phone")), pattern)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.toUpperCase()));
            }
            if (dealerId != null) {
                predicates.add(cb.equal(root.get("dealer").get("dealerId"), dealerId));
            }

            // Avoid duplicate rows from joins
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
