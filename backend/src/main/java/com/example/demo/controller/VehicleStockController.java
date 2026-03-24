package com.example.demo.controller;

import com.example.demo.dto.CreateStockRequest;
import com.example.demo.dto.TransferStockRequest;
import com.example.demo.dto.VehicleStockDTO;
import com.example.demo.service.VehicleStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class VehicleStockController {

    private final VehicleStockService stockService;

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * ADMIN gets all stock; DEALER gets only their own.
     * EMPLOYEE is forbidden (enforced in service).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<List<VehicleStockDTO>> getStock(Authentication auth) {
        return ResponseEntity.ok(stockService.getStock(auth.getName()));
    }

    @GetMapping("/{vin}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<VehicleStockDTO> getByVin(@PathVariable String vin,
                                                      Authentication auth) {
        return ResponseEntity.ok(stockService.getByVin(vin, auth.getName()));
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * DEALER: adds stock — dealerId is ALWAYS derived from JWT, never trusted from payload.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<VehicleStockDTO> addStock(@RequestBody CreateStockRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(stockService.addStock(request, auth.getName()));
    }

    /**
     * ADMIN-only: add stock for a specific dealer.
     */
    @PostMapping("/admin/dealer/{dealerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleStockDTO> addStockForDealer(@PathVariable Long dealerId,
                                                               @RequestBody CreateStockRequest request,
                                                               Authentication auth) {
        return ResponseEntity.ok(stockService.addStockForDealer(request, dealerId, auth.getName()));
    }

    // ── Status Updates ────────────────────────────────────────────────────────

    @PatchMapping("/{vin}/book")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<VehicleStockDTO> markAsBooked(@PathVariable String vin,
                                                         Authentication auth) {
        return ResponseEntity.ok(stockService.markAsBooked(vin, auth.getName()));
    }

    @PatchMapping("/{vin}/sell")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<VehicleStockDTO> markAsSold(@PathVariable String vin,
                                                       Authentication auth) {
        return ResponseEntity.ok(stockService.markAsSold(vin, auth.getName()));
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER')")
    public ResponseEntity<VehicleStockDTO> transfer(@RequestBody TransferStockRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(stockService.transferStock(request, auth.getName()));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{vin}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable String vin, Authentication auth) {
        stockService.deleteStock(vin, auth.getName());
        return ResponseEntity.ok("Stock deleted successfully");
    }
}