package com.example.demo.repository;

import com.example.demo.entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long>,
        JpaSpecificationExecutor<Enquiry> {

    List<Enquiry> findByDealerDealerIdOrderByCreatedAtDesc(Long dealerId);

    List<Enquiry> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByDealerDealerIdAndStatus(Long dealerId, String status);
}
