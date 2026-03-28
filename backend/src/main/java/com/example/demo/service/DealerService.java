package com.example.demo.service;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.Dealer;
import com.example.demo.repository.DealerRepository;
import com.example.demo.specification.DealerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("dealerName", "dealerCode", "status", "createdAt");

    // ── Read ─────────────────────────────────────────────────────────────────

    public PagedResponse<Dealer> getAllDealers(int page, int pageSize,
                                               String sortBy, String sortDirection,
                                               String keyword, String status) {
        String resolvedSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(resolvedSort).descending()
                : Sort.by(resolvedSort).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<Dealer> spec = DealerSpecification.build(keyword, status);
        Page<Dealer> result = dealerRepository.findAll(spec, pageable);

        return new PagedResponse<>(result.getContent(), page, pageSize, result.getTotalElements());
    }

    public List<Dealer> getAllDealers() {
        return dealerRepository.findAll();
    }

    public Dealer getDealerById(Long id) {
        return dealerRepository.findById(id).orElse(null);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public Dealer saveDealer(Dealer dealer) {
        if (dealer.getDealerCode() == null || dealer.getDealerCode().isEmpty()) {
            dealer.setDealerCode("DLR" + System.currentTimeMillis());
        }
        if (dealer.getCreatedAt() == null) {
            dealer.setCreatedAt(LocalDateTime.now());
        }
        if (dealer.getStatus() == null || dealer.getStatus().isEmpty()) {
            dealer.setStatus("ACTIVE");
        }
        return dealerRepository.save(dealer);
    }

    public Dealer updateDealer(Long id, Dealer dealer) {
        Dealer existing = dealerRepository.findById(id).orElseThrow();
        existing.setDealerName(dealer.getDealerName());
        existing.setDealerCode(dealer.getDealerCode());
        existing.setPhone(dealer.getPhone());
        existing.setEmail(dealer.getEmail());
        existing.setAddress(dealer.getAddress());
        existing.setCityId(dealer.getCityId());
        existing.setStateId(dealer.getStateId());
        existing.setStatus(dealer.getStatus());
        return dealerRepository.save(existing);
    }

    public void deleteDealer(Long id) {
        dealerRepository.deleteById(id);
    }
}
