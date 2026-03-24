package com.example.demo.service;

import com.example.demo.dto.CreateStockRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleStockService {

    private final VehicleStockRepository stockRepository;
    private final VehicleModelRepository modelRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * ADMIN: all stock. DEALER: only their own.
     */
    public List<VehicleStockDTO> getStock(String username) {
        User user = getUser(username);
        String role = user.getRole();

        if ("ADMIN".equals(role)) {
            return stockRepository.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }

        if ("DEALER".equals(role)) {
            Long dealerId = getDealerIdForUser(user);
            return stockRepository.findByDealerDealerId(dealerId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }

        throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
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

    /**
     * ADMIN: can add stock for any dealer (passes dealerId in payload or uses default).
     * DEALER: dealerId is ALWAYS derived from JWT — payload dealerId is ignored.
     */
    @Transactional
    public VehicleStockDTO addStock(CreateStockRequest request, String username) {
        User user = getUser(username);

        if (stockRepository.existsByVin(request.getVin())) {
            throw new IllegalArgumentException("VIN already exists: " + request.getVin());
        }

        VehicleModel model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + request.getModelId()));

        Dealer dealer;
        if ("ADMIN".equals(user.getRole())) {
            // Admin must pass dealerId via a separate field or we attach to first dealer — here we
            // require it to be sent as a request param from controller
            dealer = dealerRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No dealers found"));
        } else if ("DEALER".equals(user.getRole())) {
            // Dealer's ID is always derived from JWT — never trusted from request
            Long dealerId = getDealerIdForUser(user);
            dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found for user"));
        } else {
            throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
        }

        VehicleStock stock = VehicleStock.builder()
                .vin(request.getVin())
                .model(model)
                .dealer(dealer)
                .color(request.getColor())
                .manufactureDate(request.getManufactureDate())
                .stockStatus("AVAILABLE")
                .build();

        return toDTO(stockRepository.save(stock));
    }

    /**
     * ADMIN: can add stock for ANY specific dealer.
     */
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

        VehicleStock stock = VehicleStock.builder()
                .vin(request.getVin())
                .model(model)
                .dealer(dealer)
                .color(request.getColor())
                .manufactureDate(request.getManufactureDate())
                .stockStatus("AVAILABLE")
                .build();

        return toDTO(stockRepository.save(stock));
    }

    // ── Status Updates ───────────────────────────────────────────────────────

    /**
     * Mark a vehicle as BOOKED. Transition: AVAILABLE → BOOKED only.
     * DEALER: only their own vehicles.
     */
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

    /**
     * Mark a vehicle as SOLD. Transition: BOOKED → SOLD only.
     * DEALER: only their own vehicles.
     */
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

    /**
     * Transfer stock from one dealer to another.
     * ADMIN: can transfer any vehicle to any dealer.
     * DEALER: can only transfer their own AVAILABLE vehicles.
     */
    @Transactional
    public VehicleStockDTO transferStock(TransferStockRequest request, String username) {
        User user = getUser(username);
        VehicleStock stock = stockRepository.findById(request.getVin())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + request.getVin()));

        // Role-specific ownership check
        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!stock.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("You can only transfer your own stock.");
            }
        } else if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("EMPLOYEE role has no access to stock module.");
        }

        // Only AVAILABLE stock can be transferred
        if (!"AVAILABLE".equals(stock.getStockStatus())) {
            throw new InvalidStockStatusException(
                    "Only AVAILABLE stock can be transferred. Current: " + stock.getStockStatus());
        }

        Dealer targetDealer = dealerRepository.findById(request.getTargetDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Target dealer not found: " + request.getTargetDealerId()));

        stock.setDealer(targetDealer);
        return toDTO(stockRepository.save(stock));
    }

    // ── Delete (ADMIN only) ──────────────────────────────────────────────────

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

    /**
     * Derives the dealerId associated with a DEALER-role user.
     * In the current schema, the User entity has a dealer_id column.
     * Adjust if your User entity stores the link differently.
     */
    private Long getDealerIdForUser(User user) {
        // The User entity stores a dealer_id field (see schema).
        // If your User entity exposes it as a Dealer relationship, adapt accordingly.
        // For now we read it from a helper method:
        if (user.getDealerId() == null) {
            throw new AccessDeniedException("Dealer user is not linked to any dealership.");
        }
        return user.getDealerId();
    }

    /**
     * Fetches stock and enforces ownership for DEALER role.
     */
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