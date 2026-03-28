package com.example.demo.service;

import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.VehicleModelDTO;
import com.example.demo.entity.VehicleModel;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.VehicleModelRepository;
import com.example.demo.specification.VehicleModelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("modelName", "fuelType", "transmission", "exShowroomPrice", "status", "createdAt");

    // ── Read ─────────────────────────────────────────────────────────────────

    public PagedResponse<VehicleModelDTO> getAll(int page, int pageSize,
                                                  String sortBy, String sortDirection,
                                                  String keyword, String status) {
        String resolvedSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort).descending()
                : Sort.by(resolvedSort).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<VehicleModel> spec = VehicleModelSpecification.build(keyword, status);
        Page<VehicleModel> result = modelRepository.findAll(spec, pageable);

        List<VehicleModelDTO> content = result.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page, pageSize, result.getTotalElements());
    }

    // Kept for backward-compat (e.g. /active endpoint)
    public List<VehicleModelDTO> getAll() {
        return modelRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<VehicleModelDTO> getActiveModels() {
        return modelRepository.findByStatus("ACTIVE").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VehicleModelDTO getById(Long id) {
        VehicleModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));
        return toDTO(model);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public VehicleModelDTO create(VehicleModelDTO dto) {
        validateFuelType(dto.getFuelType());
        validateTransmission(dto.getTransmission());

        VehicleModel model = VehicleModel.builder()
                .modelName(dto.getModelName())
                .variant(dto.getVariant())
                .fuelType(dto.getFuelType().toUpperCase())
                .transmission(dto.getTransmission().toUpperCase())
                .exShowroomPrice(dto.getExShowroomPrice())
                .status(dto.getStatus() != null ? dto.getStatus().toUpperCase() : "ACTIVE")
                .build();
        return toDTO(modelRepository.save(model));
    }

    public VehicleModelDTO update(Long id, VehicleModelDTO dto) {
        VehicleModel existing = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + id));

        if (dto.getModelName() != null)       existing.setModelName(dto.getModelName());
        if (dto.getVariant() != null)         existing.setVariant(dto.getVariant());
        if (dto.getFuelType() != null) {
            validateFuelType(dto.getFuelType());
            existing.setFuelType(dto.getFuelType().toUpperCase());
        }
        if (dto.getTransmission() != null) {
            validateTransmission(dto.getTransmission());
            existing.setTransmission(dto.getTransmission().toUpperCase());
        }
        if (dto.getExShowroomPrice() != null) existing.setExShowroomPrice(dto.getExShowroomPrice());
        if (dto.getStatus() != null)          existing.setStatus(dto.getStatus().toUpperCase());

        return toDTO(modelRepository.save(existing));
    }

    public void delete(Long id) {
        if (!modelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Model not found with id: " + id);
        }
        modelRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateFuelType(String fuelType) {
        if (!List.of("PETROL", "DIESEL", "EV").contains(fuelType.toUpperCase())) {
            throw new IllegalArgumentException("Invalid fuelType. Allowed: PETROL, DIESEL, EV");
        }
    }

    private void validateTransmission(String transmission) {
        if (!List.of("MANUAL", "AUTOMATIC").contains(transmission.toUpperCase())) {
            throw new IllegalArgumentException("Invalid transmission. Allowed: MANUAL, AUTOMATIC");
        }
    }

    public VehicleModelDTO toDTO(VehicleModel m) {
        return VehicleModelDTO.builder()
                .modelId(m.getModelId())
                .modelName(m.getModelName())
                .variant(m.getVariant())
                .fuelType(m.getFuelType())
                .transmission(m.getTransmission())
                .exShowroomPrice(m.getExShowroomPrice())
                .status(m.getStatus())
                .build();
    }
}
