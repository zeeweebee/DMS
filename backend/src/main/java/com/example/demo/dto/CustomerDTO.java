package com.example.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    private Long customerId;
    private String customerName;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
}
