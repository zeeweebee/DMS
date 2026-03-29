package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.PagedResponse;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * GET /api/customers
     * Query params: page, pageSize, sortBy, sortDirection, keyword
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerDTO>>> getAll(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDirection,
            @RequestParam(required = false)           String keyword) {

        return ResponseEntity.ok(ApiResponse.ok(
                customerService.getAll(page, pageSize, sortBy, sortDirection, keyword)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> create(@RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> update(@PathVariable Long id,
                                                            @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.update(id, dto)));
    }
}
