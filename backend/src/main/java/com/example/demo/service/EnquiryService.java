package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.specification.EnquirySpecification;
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
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerService customerService;
    private final VehicleModelRepository modelRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;

    private static final Set<String> VALID_STATUSES =
            Set.of("NEW", "CONTACTED", "TEST_DRIVE", "NEGOTIATING", "CONVERTED", "LOST");

    // ── Read ─────────────────────────────────────────────────────────────────

    public PagedResponse<EnquiryDTO> getAll(String username,
                                             int page, int pageSize,
                                             String sortBy, String sortDirection,
                                             String keyword, String status,
                                             Long filterDealerId) {
        User user = getUser(username);
        Long effectiveDealerId = resolveDealer(user, filterDealerId);

        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort(sortBy)).descending()
                : Sort.by(resolvedSort(sortBy)).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<Enquiry> spec = EnquirySpecification.build(keyword, status, effectiveDealerId);
        Page<Enquiry> result = enquiryRepository.findAll(spec, pageable);

        List<EnquiryDTO> content = result.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());

        return new PagedResponse<>(content, page, pageSize, result.getTotalElements());
    }

    public EnquiryDTO getById(Long id, String username) {
        User user = getUser(username);
        Enquiry enquiry = findOrThrow(id);
        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!enquiry.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("You can only view your own dealer's enquiries.");
            }
        }
        return toDTO(enquiry);
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public EnquiryDTO create(CreateEnquiryRequest req, String username) {
        User user = getUser(username);

        // Resolve customer: use existing or create/find by phone+email
        Customer customer;
        if (req.getCustomerId() != null) {
            customer = customerService.findEntityById(req.getCustomerId());
        } else {
            if (req.getCustomerName() == null || req.getCustomerName().isBlank()) {
                throw new IllegalArgumentException("customerName is required when customerId is not provided.");
            }
            CustomerDTO dto = customerService.findOrCreate(
                    req.getCustomerName(), req.getCustomerPhone(), req.getCustomerEmail());
            customer = customerService.findEntityById(dto.getCustomerId());
        }

        // Resolve model
        VehicleModel model = modelRepository.findById(req.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + req.getModelId()));

        // Resolve dealer
        Long dealerId = resolveDealerForWrite(user, req.getDealerId());
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + dealerId));

        String status = (req.getStatus() != null && VALID_STATUSES.contains(req.getStatus().toUpperCase()))
                ? req.getStatus().toUpperCase() : "NEW";

        Enquiry enquiry = Enquiry.builder()
                .customer(customer)
                .model(model)
                .dealer(dealer)
                .source(req.getSource())
                .enquiryDate(req.getEnquiryDate() != null ? req.getEnquiryDate() : LocalDate.now())
                .status(status)
                .build();

        return toDTO(enquiryRepository.save(enquiry));
    }

    // ── Update Status ─────────────────────────────────────────────────────────

    @Transactional
    public EnquiryDTO updateStatus(Long id, String newStatus, String username) {
        User user = getUser(username);
        Enquiry enquiry = findOrThrow(id);

        if ("DEALER".equals(user.getRole())) {
            Long dealerId = getDealerIdForUser(user);
            if (!enquiry.getDealer().getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("Access denied.");
            }
        }
        if (!VALID_STATUSES.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status: " + newStatus
                    + ". Allowed: " + VALID_STATUSES);
        }
        enquiry.setStatus(newStatus.toUpperCase());
        return toDTO(enquiryRepository.save(enquiry));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id, String username) {
        User user = getUser(username);
        if (!"ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("Only ADMIN can delete enquiries.");
        }
        if (!enquiryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enquiry not found: " + id);
        }
        enquiryRepository.deleteById(id);
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

    private Long resolveDealerForWrite(User user, Long requested) {
        if ("DEALER".equals(user.getRole())) return getDealerIdForUser(user);
        if (requested == null) throw new IllegalArgumentException("dealerId is required for ADMIN.");
        return requested;
    }

    private String resolvedSort(String sortBy) {
        return List.of("enquiryDate", "status", "createdAt").contains(sortBy) ? sortBy : "createdAt";
    }

    private Enquiry findOrThrow(Long id) {
        return enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + id));
    }

    public EnquiryDTO toDTO(Enquiry e) {
        return EnquiryDTO.builder()
                .enquiryId(e.getEnquiryId())
                .customerId(e.getCustomer().getCustomerId())
                .customerName(e.getCustomer().getCustomerName())
                .customerPhone(e.getCustomer().getPhone())
                .customerEmail(e.getCustomer().getEmail())
                .modelId(e.getModel().getModelId())
                .modelName(e.getModel().getModelName())
                .variant(e.getModel().getVariant())
                .dealerId(e.getDealer().getDealerId())
                .dealerName(e.getDealer().getDealerName())
                .source(e.getSource())
                .enquiryDate(e.getEnquiryDate())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public Enquiry findEntityById(Long id) {
        return findOrThrow(id);
    }
}
