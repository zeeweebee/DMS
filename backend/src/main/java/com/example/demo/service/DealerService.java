package com.example.demo.service;

import com.example.demo.entity.Dealer;
import com.example.demo.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;

    // ✅ Fix: auto-generate dealerCode, set createdAt and status
    public Dealer saveDealer(Dealer dealer){
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

    public List<Dealer> getAllDealers(){
        return dealerRepository.findAll();
    }

    public Dealer getDealerById(Long id){
        return dealerRepository.findById(id).orElse(null);
    }

    public Dealer updateDealer(Long id, Dealer dealer){
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

    public void deleteDealer(Long id){
        dealerRepository.deleteById(id);
    }
}