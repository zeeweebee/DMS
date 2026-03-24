package com.example.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferStockRequest {
    private String vin;
    private Long targetDealerId;
}