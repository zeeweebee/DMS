package com.example.demo.service;

import com.example.demo.dto.CreateStockRequest;
import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.TransferStockRequest;
import com.example.demo.dto.VehicleStockDTO;
import com.example.demo.entity.Dealer;
import com.example.demo.entity.User;
import com.example.demo.entity.VehicleModel;
import com.example.demo.entity.VehicleStock;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.InvalidStockStatusException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DealerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleModelRepository;
import com.example.demo.repository.VehicleStockRepository;
import com.example.demo.specification.VehicleStockSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleStockService {

    private final VehicleStockRepository stockRepository;
    private final VehicleModelRepository modelRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("vin", "stockStatus", "color", "manufactureDate", "createdAt");

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * ADMIN: all stock (with optional filters).
     * DEALER: only their own stock (dealerId always forced from JWT).
     */
    public PagedResponse<VehicleStockDTO> getStock(String username,
                                                    int page, int pageSize,
                                                    String sortBy, String sortDirection,
                                                    String keyword, String stockStatus,
                                                    Long filterDealerId, Long modelId) {
        User user = getUser(username);
        String role = user.getRole();

        if ("EMPLOYEE".equals(role)) {
            throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
        }

        String resolvedSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort).descending()
                : Sort.by(resolvedSort).ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        // DEALER: always scope to their own dealerId, ignore any filterDealerId from request
        Long effectiveDealerId = "DEALER".equals(role) ? getDealerIdForUser(user) : filterDealerId;

        Specification<VehicleStock> spec =
                VehicleStockSpecification.build(keyword, stockStatus, effectiveDealerId, modelId);

        Page<VehicleStock> result = stockRepository.findAll(spec, pageable);
        List<VehicleStockDTO> content = result.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page, pageSize, result.getTotalElements());
    }

    public VehicleStockDTO getByVin(String vin, String username) {
        User user = getUser(username);
        VehicleStock stock = stockRepository.findById(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for VIN: " + vin));

        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!stock.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("You can only view your own stock.");
            }
        }
        return toDTO(stock);
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public VehicleStockDTO addStock(CreateStockRequest request, String username) {
        User user = getUser(username);

        if (stockRepository.existsByVin(request.getVin())) {
            throw new IllegalArgumentException("VIN already exists: " + request.getVin());
        }

        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + request.getModelId()));

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("ADMIN must use /api/stock/admin/dealer/{dealerId} to add stock for a specific dealer.");
        } else if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found for user"));
            return toDTO(stockRepository.save(buildStock(request, model, dealer)));
        } else {
            throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
        }
    }

    @Transactional
    public VehicleStockDTO addStockForDealer(CreateStockRequest request, Long dealerId, String username) {
        User user = getUser(username);
        if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("Only ADMIN can assign stock to a specific dealer.");
        }
        if (stockRepository.existsByVin(request.getVin())) {
            throw new IllegalArgumentException("VIN already exists: " + request.getVin());
        }
        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + request.getModelId()));
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + dealerId));
        return toDTO(stockRepository.save(buildStock(request, model, dealer)));
    }

    // ── Status Updates ───────────────────────────────────────────────────────

    @Transactional
    public VehicleStockDTO markAsBooked(String vin, String username) {
        VehicleStock stock = getStockWithOwnershipCheck(vin, username);
        if (!"AVAILABLE".equals(stock.getStockStatus())) {
            throw new InvalidStockStatusException(
                    "Can only book AVAILABLE vehicles. Current status: " + stock.getStockStatus());
        }
        stock.setStockStatus("BOOKED");
        return toDTO(stockRepository.save(stock));
    }

    @Transactional
    public VehicleStockDTO markAsSold(String vin, String username) {
        VehicleStock stock = getStockWithOwnershipCheck(vin, username);
        if (!"BOOKED".equals(stock.getStockStatus())) {
            throw new InvalidStockStatusException(
                    "Can only sell BOOKED vehicles. Current status: " + stock.getStockStatus());
        }
        stock.setStockStatus("SOLD");
        return toDTO(stockRepository.save(stock));
    }

    // ── Transfer ─────────────────────────────────────────────────────────────

    @Transactional
    public VehicleStockDTO transferStock(TransferStockRequest request, String username) {
        User user = getUser(username);
        VehicleStock stock = stockRepository.findById(request.getVin())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + request.getVin()));

        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!stock.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("You can only transfer your own stock.");
            }
        } else if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
        }

        if (!"AVAILABLE".equals(stock.getStockStatus())) {
            throw new InvalidStockStatusException(
                    "Only AVAILABLE stock can be transferred. Current: " + stock.getStockStatus());
        }

        Dealer targetDealer = dealerRepository.findById(request.getTargetDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Target dealer not found: " + request.getTargetDealerId()));

        stock.setDealer(targetDealer);
        return toDTO(stockRepository.save(stock));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void deleteStock(String vin, String username) {
        User user = getUser(username);
        if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("Only ADMIN can delete stock.");
        }
        if (!stockRepository.existsById(vin)) {
            throw new ResourceNotFoundException("Stock not found: " + vin);
        }
        stockRepository.deleteById(vin);
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Long getDealerIdForUser(User user) {
        if (user.getDealerId() == null) {
            throw new AccessDeniedException("Dealer user is not linked to any dealership.");
        }
        return user.getDealerId();
    }

    private VehicleStock getStockWithOwnershipCheck(String vin, String username) {
        User user = getUser(username);
        VehicleStock stock = stockRepository.findById(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + vin));

        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!stock.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("You can only update your own stock.");
            }
        } else if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
        }
        return stock;
    }

    private VehicleStock buildStock(CreateStockRequest request, VehicleModel model, Dealer dealer) {
        return VehicleStock.builder()
                .vin(request.getVin())
                .model(model)
                .dealer(dealer)
                .color(request.getColor())
                .manufactureDate(request.getManufactureDate())
                .stockStatus("AVAILABLE")
                .build();
    }

    public VehicleStockDTO toDTO(VehicleStock s) {
        return VehicleStockDTO.builder()
                .vin(s.getVin())
                .modelId(s.getModel().getModelId())
                .modelName(s.getModel().getModelName())
                .variant(s.getModel().getVariant())
                .dealerId(s.getDealer().getDealerId())
                .dealerName(s.getDealer().getDealerName())
                .color(s.getColor())
                .manufactureDate(s.getManufactureDate())
                .stockStatus(s.getStockStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
