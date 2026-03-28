package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.Dealer;
import com.example.demo.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

    /**
     * GET /api/dealers
     *
     * Query params:
     *   page          (default 0)
     *   pageSize      (default 10)
     *   sortBy        (default createdAt) — allowed: dealerName, dealerCode, status, createdAt
     *   sortDirection (default asc)
     *   keyword       — searches dealerName, dealerCode
     *   status        — filter by ACTIVE / INACTIVE
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<Dealer>>> getAllDealers(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDirection,
            @RequestParam(required = false)      String keyword,
            @RequestParam(required = false)      String status) {

        return ResponseEntity.ok(ApiResponse.ok(
                dealerService.getAllDealers(page, pageSize, sortBy, sortDirection, keyword, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Dealer>> getDealer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(dealerService.getDealerById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Dealer>> createDealer(@RequestBody Dealer dealer) {
        return ResponseEntity.ok(ApiResponse.ok(dealerService.saveDealer(dealer)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Dealer>> updateDealer(@PathVariable Long id,
                                                             @RequestBody Dealer dealer) {
        return ResponseEntity.ok(ApiResponse.ok(dealerService.updateDealer(id, dealer)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok(ApiResponse.ok("Dealer deleted", null));
    }
}
