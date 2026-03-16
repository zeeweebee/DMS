package com.example.demo.service;

import com.example.demo.entity.Dealer;
import com.example.demo.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealerService {

    private final DealerRepository dealerRepository;

    public DealerService(DealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    public Dealer saveDealer(Dealer dealer){
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

        return dealerRepository.save(existing);
    }

    public void deleteDealer(Long id){
        dealerRepository.deleteById(id);
    }
}