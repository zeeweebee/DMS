package com.example.demo.service;

import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.Customer;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.specification.CustomerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    // ── Read ─────────────────────────────────────────────────────────────────

    public PagedResponse<CustomerDTO> getAll(int page, int pageSize,
                                             String sortBy, String sortDirection,
                                             String keyword) {
        String resolvedSort = List.of("customerName", "phone", "email", "createdAt")
                .contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort).descending()
                : Sort.by(resolvedSort).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<Customer> spec = CustomerSpecification.build(keyword);
        Page<Customer> result = customerRepository.findAll(spec, pageable);

        List<CustomerDTO> content = result.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());

        return new PagedResponse<>(content, page, pageSize, result.getTotalElements());
    }

    public CustomerDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    /**
     * Duplicate detection: returns existing customer if phone or email already exists.
     * Otherwise creates a new one.
     */
    public CustomerDTO findOrCreate(String name, String phone, String email) {
        // Check phone first, then email
        if (phone != null && !phone.isBlank()) {
            Optional<Customer> byPhone = customerRepository.findByPhone(phone.trim());
            if (byPhone.isPresent()) return toDTO(byPhone.get());
        }
        if (email != null && !email.isBlank()) {
            Optional<Customer> byEmail = customerRepository.findByEmail(email.trim());
            if (byEmail.isPresent()) return toDTO(byEmail.get());
        }

        Customer newCustomer = Customer.builder()
                .customerName(name)
                .phone(phone != null ? phone.trim() : null)
                .email(email != null ? email.trim() : null)
                .build();
        return toDTO(customerRepository.save(newCustomer));
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public CustomerDTO create(CustomerDTO dto) {
        // Validate uniqueness
        if (dto.getPhone() != null && customerRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new IllegalArgumentException("A customer with phone " + dto.getPhone() + " already exists.");
        }
        if (dto.getEmail() != null && customerRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A customer with email " + dto.getEmail() + " already exists.");
        }
        Customer c = Customer.builder()
                .customerName(dto.getCustomerName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .build();
        return toDTO(customerRepository.save(c));
    }

    public CustomerDTO update(Long id, CustomerDTO dto) {
        Customer existing = findOrThrow(id);
        if (dto.getCustomerName() != null) existing.setCustomerName(dto.getCustomerName());
        if (dto.getPhone() != null)        existing.setPhone(dto.getPhone());
        if (dto.getEmail() != null)        existing.setEmail(dto.getEmail());
        return toDTO(customerRepository.save(existing));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public CustomerDTO toDTO(Customer c) {
        return CustomerDTO.builder()
                .customerId(c.getCustomerId())
                .customerName(c.getCustomerName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public Customer findEntityById(Long id) {
        return findOrThrow(id);
    }
}
