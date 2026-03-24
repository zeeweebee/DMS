package com.example.demo.controller;

import com.example.demo.dto.VehicleModelDTO;
import com.example.demo.service.VehicleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService modelService;

    /** All authenticated roles can view models */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','EMPLOYEE')")
    public ResponseEntity<List<VehicleModelDTO>> getAll() {
        return ResponseEntity.ok(modelService.getAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','EMPLOYEE')")
    public ResponseEntity<List<VehicleModelDTO>> getActive() {
        return ResponseEntity.ok(modelService.getActiveModels());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEALER','EMPLOYEE')")
    public ResponseEntity<VehicleModelDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(modelService.getById(id));
    }

    /** ADMIN-only mutations */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleModelDTO> create(@RequestBody VehicleModelDTO dto) {
        return ResponseEntity.ok(modelService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleModelDTO> update(@PathVariable Long id,
                                                   @RequestBody VehicleModelDTO dto) {
        return ResponseEntity.ok(modelService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        modelService.delete(id);
        return ResponseEntity.ok("Model deleted successfully");
    }
}