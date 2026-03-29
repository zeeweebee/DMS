package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.InvalidStockStatusException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.specification.BookingSpecification;
import com.example.demo.specification.VehicleStockSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EnquiryRepository enquiryRepository;
    private final VehicleStockRepository stockRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;

    // ── Read ─────────────────────────────────────────────────────────────────

    public PagedResponse<BookingDTO> getAll(String username,
                                             int page, int pageSize,
                                             String sortBy, String sortDirection,
                                             String status, Long filterDealerId) {
        User user = getUser(username);
        Long effectiveDealerId = resolveDealer(user, filterDealerId);

        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort(sortBy)).descending()
                : Sort.by(resolvedSort(sortBy)).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<Booking> spec = BookingSpecification.build(status, effectiveDealerId);
        Page<Booking> result = bookingRepository.findAll(spec, pageable);

        List<BookingDTO> content = result.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());

        return new PagedResponse<>(content, page, pageSize, result.getTotalElements());
    }

    public BookingDTO getById(Long id, String username) {
        User user = getUser(username);
        Booking booking = findOrThrow(id);
        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!booking.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("You can only view your own dealer's bookings.");
            }
        }
        return toDTO(booking);
    }

    // ── Create (with FIFO VIN allocation) ────────────────────────────────────

    @Transactional
    public BookingDTO create(CreateBookingRequest req, String username) {
        User user = getUser(username);

        // Resolve the enquiry
        Enquiry enquiry = enquiryRepository.findById(req.getEnquiryId())
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + req.getEnquiryId()));

        // Check dealer access
        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!enquiry.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("Enquiry does not belong to your dealership.");
            }
        }

        // Resolve VIN: explicit or FIFO
        VehicleStock stock;
        if (req.getVin() != null && !req.getVin().isBlank()) {
            stock = stockRepository.findById(req.getVin())
                    .orElseThrow(() -> new ResourceNotFoundException("VIN not found: " + req.getVin()));
        } else {
            // FIFO: oldest AVAILABLE stock matching the enquiry's model at the dealer
            Specification<VehicleStock> fifoSpec = VehicleStockSpecification.build(
                    null, "AVAILABLE",
                    enquiry.getDealer().getDealerId(),
                    enquiry.getModel().getModelId());
            Sort fifo = Sort.by("createdAt").ascending();
            List<VehicleStock> candidates = stockRepository.findAll(fifoSpec, fifo);
            if (candidates.isEmpty()) {
                throw new InvalidStockStatusException(
                        "No AVAILABLE stock for model '" + enquiry.getModel().getModelName()
                        + "' at dealer '" + enquiry.getDealer().getDealerName() + "'.");
            }
            stock = candidates.get(0);
        }

        // Validate the selected stock
        if (!"AVAILABLE".equals(stock.getStockStatus())) {
            throw new InvalidStockStatusException(
                    "VIN " + stock.getVin() + " is not AVAILABLE (current: " + stock.getStockStatus() + ").");
        }

        // Guard: no active booking on this VIN already
        if (bookingRepository.existsByVehicleStockVinAndBookingStatusNot(stock.getVin(), "CANCELLED")) {
            throw new IllegalArgumentException("VIN " + stock.getVin() + " already has an active booking.");
        }

        // Transition stock status
        stock.setStockStatus("BOOKED");
        stockRepository.save(stock);

        // Advance the enquiry to CONVERTED
        enquiry.setStatus("CONVERTED");
        enquiryRepository.save(enquiry);

        Booking booking = Booking.builder()
                .enquiry(enquiry)
                .vehicleStock(stock)
                .dealer(enquiry.getDealer())
                .bookingDate(req.getBookingDate() != null ? req.getBookingDate() : LocalDate.now())
                .bookingAmount(req.getBookingAmount())
                .bookingStatus("CONFIRMED")
                .build();

        return toDTO(bookingRepository.save(booking));
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Transactional
    public BookingDTO cancel(Long id, CancelBookingRequest req, String username) {
        User user = getUser(username);
        Booking booking = findOrThrow(id);

        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!booking.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("Access denied.");
            }
        }
        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new IllegalArgumentException("Booking is already cancelled.");
        }

        // Revert stock to AVAILABLE
        VehicleStock stock = booking.getVehicleStock();
        stock.setStockStatus("AVAILABLE");
        stockRepository.save(stock);

        // Revert enquiry status
        Enquiry enquiry = booking.getEnquiry();
        enquiry.setStatus("NEGOTIATING");
        enquiryRepository.save(enquiry);

        booking.setBookingStatus("CANCELLED");
        booking.setCancellationReason(req.getCancellationReason());
        booking.setRefundAmount(req.getRefundAmount());

        return toDTO(bookingRepository.save(booking));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Long getDealerIdForUser(User user) {
        if (user.getDealerId() == null) throw new AccessDeniedException("Dealer user not linked to a dealership.");
        return user.getDealerId();
    }

    private Long resolveDealer(User user, Long requested) {
        return "DEALER".equals(user.getRole()) ? getDealerIdForUser(user) : requested;
    }

    private String resolvedSort(String s) {
        return List.of("bookingDate", "bookingStatus", "bookingAmount", "createdAt").contains(s) ? s : "createdAt";
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    public BookingDTO toDTO(Booking b) {
        VehicleStock s = b.getVehicleStock();
        return BookingDTO.builder()
                .bookingId(b.getBookingId())
                .enquiryId(b.getEnquiry().getEnquiryId())
                .customerId(b.getEnquiry().getCustomer().getCustomerId())
                .customerName(b.getEnquiry().getCustomer().getCustomerName())
                .customerPhone(b.getEnquiry().getCustomer().getPhone())
                .vin(s.getVin())
                .modelName(s.getModel().getModelName())
                .variant(s.getModel().getVariant())
                .color(s.getColor())
                .dealerId(b.getDealer().getDealerId())
                .dealerName(b.getDealer().getDealerName())
                .bookingDate(b.getBookingDate())
                .bookingAmount(b.getBookingAmount())
                .bookingStatus(b.getBookingStatus())
                .cancellationReason(b.getCancellationReason())
                .refundAmount(b.getRefundAmount())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
