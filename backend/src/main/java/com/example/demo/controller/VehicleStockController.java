package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CreateStockRequest;
import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.TransferStockRequest;
import com.example.demo.dto.VehicleStockDTO;
import com.example.demo.service.VehicleStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class VehicleStockController {

    private final VehicleStockService stockService;

    /**
     * GET /api/stock
     *
     * Query params:
     *   page          (default 0)
     *   pageSize      (default 10)
     *   sortBy        (default createdAt) — allowed: vin, stockStatus, color, manufactureDate, createdAt
     *   sortDirection (default asc)
     *   keyword       — searches VIN (partial match)
     *   stockStatus   — filter: AVAILABLE / BOOKED / SOLD
     *   dealerId      — filter by dealer (ADMIN only; ignored for DEALER role)
     *   modelId       — filter by model
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleStockDTO>>> getStock(
            Authentication auth,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDirection,
            @RequestParam(required = false)      String keyword,
            @RequestParam(required = false)      String stockStatus,
            @RequestParam(required = false)      Long dealerId,
            @RequestParam(required = false)      Long modelId) {

        return ResponseEntity.ok(ApiResponse.ok(
                stockService.getStock(auth.getName(), page, pageSize, sortBy, sortDirection,
                        keyword, stockStatus, dealerId, modelId)));
    }

    @GetMapping("/{vin}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<VehicleStockDTO>> getByVin(@PathVariable String vin,
                                                                   Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getByVin(vin, auth.getName())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<VehicleStockDTO>> addStock(@RequestBody CreateStockRequest request,
                                                                  Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.addStock(request, auth.getName())));
    }

    @PostMapping("/admin/dealer/{dealerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VehicleStockDTO>> addStockForDealer(@PathVariable Long dealerId,
                                                                            @RequestBody CreateStockRequest request,
                                                                            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.addStockForDealer(request, dealerId, auth.getName())));
    }

    @PatchMapping("/{vin}/book")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<VehicleStockDTO>> markAsBooked(@PathVariable String vin,
                                                                       Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.markAsBooked(vin, auth.getName())));
    }

    @PatchMapping("/{vin}/sell")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<VehicleStockDTO>> markAsSold(@PathVariable String vin,
                                                                     Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.markAsSold(vin, auth.getName())));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<ApiResponse<VehicleStockDTO>> transfer(@RequestBody TransferStockRequest request,
                                                                  Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.transferStock(request, auth.getName())));
    }

    @DeleteMapping("/{vin}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String vin, Authentication auth) {
        stockService.deleteStock(vin, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Stock deleted successfully", null));
    }
}
