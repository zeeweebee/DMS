package com.example.demo.controller;

import com.example.demo.entity.Dealer;
import com.example.demo.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Dealer createDealer(@RequestBody Dealer dealer){
        return dealerService.saveDealer(dealer);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Dealer> getAllDealers(){
        return dealerService.getAllDealers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Dealer getDealer(@PathVariable Long id){
        return dealerService.getDealerById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Dealer updateDealer(@PathVariable Long id,
                               @RequestBody Dealer dealer){
        return dealerService.updateDealer(id, dealer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDealer(@PathVariable Long id){
        dealerService.deleteDealer(id);
        return "Dealer deleted";
    }
}