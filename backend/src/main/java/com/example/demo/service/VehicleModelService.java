package com.example.demo.service;

import com.example.demo.dto.VehicleModelDTO;
import com.example.demo.entity.VehicleModel;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;

    // ── Read (all roles) ────────────────────────────────────────────────────

    public List<VehicleModelDTO> getAll() {
        return modelRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

    // ── Write (ADMIN only — enforced at controller level too) ───────────────

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

        if (dto.getModelName() != null)     existing.setModelName(dto.getModelName());
        if (dto.getVariant() != null)       existing.setVariant(dto.getVariant());
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

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void validateFuelType(String fuelType) {
        List<String> valid = List.of("PETROL", "DIESEL", "EV");
        if (!valid.contains(fuelType.toUpperCase())) {
            throw new IllegalArgumentException("Invalid fuelType. Allowed: PETROL, DIESEL, EV");
        }
    }

    private void validateTransmission(String transmission) {
        List<String> valid = List.of("MANUAL", "AUTOMATIC");
        if (!valid.contains(transmission.toUpperCase())) {
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