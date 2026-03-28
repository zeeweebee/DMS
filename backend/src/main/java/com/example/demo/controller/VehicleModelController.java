package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.VehicleModelDTO;
import com.example.demo.service.VehicleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService modelService;

    /**
     * GET /api/models
     *
     * Query params:
     *   page          (default 0)
     *   pageSize      (default 10)
     *   sortBy        (default createdAt) — allowed: modelName, fuelType, transmission, exShowroomPrice, status, createdAt
     *   sortDirection (default asc)
     *   keyword       — searches modelName
     *   status        — filter by ACTIVE / INACTIVE
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleModelDTO>>> getAll(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDirection,
            @RequestParam(required = false)      String keyword,
            @RequestParam(required = false)      String status) {

        return ResponseEntity.ok(ApiResponse.ok(
                modelService.getAll(page, pageSize, sortBy, sortDirection, keyword, status)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<VehicleModelDTO>>> getActive() {
        return ResponseEntity.ok(ApiResponse.ok(modelService.getActiveModels()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<VehicleModelDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(modelService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VehicleModelDTO>> create(@RequestBody VehicleModelDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(modelService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VehicleModelDTO>> update(@PathVariable Long id,
                                                                @RequestBody VehicleModelDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(modelService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        modelService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Model deleted successfully", null));
    }
}
