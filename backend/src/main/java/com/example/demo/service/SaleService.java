package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.PagedResponse;
import com.example.demo.dto.SaleDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.InvalidStockStatusException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.specification.SaleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository          saleRepository;
    private final BookingRepository       bookingRepository;
    private final VehicleStockRepository  stockRepository;
    private final UserRepository          userRepository;
    private final InvoiceSequenceService  invoiceSequenceService;
    private final InvoicePdfService       invoicePdfService;

    private static final Set<String> VALID_PAYMENT_MODES =
            Set.of("CASH", "LOAN", "EXCHANGE", "MIXED");

    // ── Read ─────────────────────────────────────────────────────────────────

    public PagedResponse<SaleDTO> getAll(String username,
                                          int page, int pageSize,
                                          String sortBy, String sortDirection,
                                          String keyword, String paymentStatus,
                                          Long filterDealerId) {
        User user = getUser(username);
        Long effectiveDealerId = resolveDealer(user, filterDealerId);

        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort(sortBy)).descending()
                : Sort.by(resolvedSort(sortBy)).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<Sale> spec = SaleSpecification.build(keyword, paymentStatus, effectiveDealerId);
        Page<Sale> result = saleRepository.findAll(spec, pageable);

        List<SaleDTO> content = result.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());

        return new PagedResponse<>(content, page, pageSize, result.getTotalElements());
    }

    public SaleDTO getById(Long id, String username) {
        User user = getUser(username);
        Sale sale = findOrThrow(id);
        assertDealerAccess(user, sale.getBooking().getDealer().getDealerId());
        return toDTO(sale);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Converts a CONFIRMED booking into a Sale:
     * 1. Validates the booking is CONFIRMED and not already sold.
     * 2. Atomically reserves the next invoice number.
     * 3. Marks the VehicleStock as SOLD.
     * 4. Marks the Booking paymentStatus (indirectly via Sale).
     * 5. Persists the Sale entity.
     */
    @Transactional
    public SaleDTO create(CreateSaleRequest req, String username) {
        User user = getUser(username);

        // Load and validate the booking
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + req.getBookingId()));

        if ("DEALER".equals(user.getRole())) {
            assertDealerAccess(user, booking.getDealer().getDealerId());
        }
        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new InvalidStockStatusException(
                    "Only CONFIRMED bookings can be converted to a sale. "
                    + "Current status: " + booking.getBookingStatus());
        }
        if (saleRepository.existsByBookingBookingId(req.getBookingId())) {
            throw new IllegalArgumentException(
                    "A sale already exists for booking #" + req.getBookingId());
        }

        // Validate payment mode
        if (req.getPaymentMode() != null
                && !VALID_PAYMENT_MODES.contains(req.getPaymentMode().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid paymentMode. Allowed: " + VALID_PAYMENT_MODES);
        }

        // Transition VehicleStock: BOOKED → SOLD
        VehicleStock stock = booking.getVehicleStock();
        if (!"BOOKED".equals(stock.getStockStatus())) {
            throw new InvalidStockStatusException(
                    "Expected stock status BOOKED but found: " + stock.getStockStatus());
        }
        stock.setStockStatus("SOLD");
        stockRepository.save(stock);

        // Reserve the invoice number (in its own transaction so lock is released immediately)
        String invoiceNumber = invoiceSequenceService.nextInvoiceNumber();

        Sale sale = Sale.builder()
                .booking(booking)
                .vehicleStock(stock)
                .invoiceNumber(invoiceNumber)
                .saleDate(req.getSaleDate() != null ? req.getSaleDate() : LocalDate.now())
                .salePrice(req.getSalePrice())
                .paymentMode(req.getPaymentMode() != null
                        ? req.getPaymentMode().toUpperCase() : "CASH")
                .loanAmount(req.getLoanAmount())
                .financeBank(req.getFinanceBank())
                .exchangeVehicle(req.getExchangeVehicle())
                .exchangeValue(req.getExchangeValue())
                .remarks(req.getRemarks())
                .paymentStatus("PAID")
                .build();

        return toDTO(saleRepository.save(sale));
    }

    // ── PDF Export ─────────────────────────────────────────────────────────

    /**
     * Returns the invoice as a PDF byte array.
     * The caller (controller) sets Content-Type and Content-Disposition.
     */
    public byte[] generateInvoicePdf(Long saleId, String username) {
        User user = getUser(username);
        Sale sale = findOrThrow(saleId);
        assertDealerAccess(user, sale.getBooking().getDealer().getDealerId());
        return invoicePdfService.generate(toDTO(sale));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Long getDealerIdForUser(User user) {
        if (user.getDealerId() == null)
            throw new AccessDeniedException("Dealer user not linked to a dealership.");
        return user.getDealerId();
    }

    private Long resolveDealer(User user, Long requested) {
        return "DEALER".equals(user.getRole()) ? getDealerIdForUser(user) : requested;
    }

    private void assertDealerAccess(User user, Long saleDealerId) {
        if ("DEALER".equals(user.getRole())) {
            Long myDealerId = getDealerIdForUser(user);
            if (!myDealerId.equals(saleDealerId)) {
                throw new AccessDeniedException("You can only access your own dealer's sales.");
            }
        }
    }

    private String resolvedSort(String s) {
        return List.of("invoiceNumber", "saleDate", "salePrice", "paymentStatus", "createdAt")
                .contains(s) ? s : "createdAt";
    }

    private Sale findOrThrow(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
    }

    public SaleDTO toDTO(Sale s) {
        Booking b  = s.getBooking();
        Enquiry e  = b.getEnquiry();
        Customer c = e.getCustomer();
        VehicleStock vs = s.getVehicleStock();
        VehicleModel m  = vs.getModel();
        Dealer d        = b.getDealer();

        return SaleDTO.builder()
                .saleId(s.getSaleId())
                .bookingId(b.getBookingId())
                .invoiceNumber(s.getInvoiceNumber())
                .saleDate(s.getSaleDate())
                .paymentStatus(s.getPaymentStatus())
                // customer
                .customerId(c.getCustomerId())
                .customerName(c.getCustomerName())
                .customerPhone(c.getPhone())
                .customerEmail(c.getEmail())
                // vehicle
                .vin(vs.getVin())
                .modelName(m.getModelName())
                .variant(m.getVariant())
                .fuelType(m.getFuelType())
                .transmission(m.getTransmission())
                .color(vs.getColor())
                .manufactureDate(vs.getManufactureDate() != null
                        ? vs.getManufactureDate().toString() : null)
                // dealer
                .dealerId(d.getDealerId())
                .dealerName(d.getDealerName())
                .dealerAddress(d.getAddress())
                .dealerPhone(d.getPhone())
                .dealerEmail(d.getEmail())
                // financials
                .exShowroomPrice(m.getExShowroomPrice())
                .bookingAmount(b.getBookingAmount())
                .salePrice(s.getSalePrice())
                .paymentMode(s.getPaymentMode())
                .loanAmount(s.getLoanAmount())
                .financeBank(s.getFinanceBank())
                .exchangeVehicle(s.getExchangeVehicle())
                .exchangeValue(s.getExchangeValue())
                .remarks(s.getRemarks())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
