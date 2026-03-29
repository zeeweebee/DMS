package com.example.demo.repository;

import com.example.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByPhoneOrEmail(String phone, String email);
}
