package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    @Column(name = "password_hash")
    private String password;

    private String role;

    /** Links DEALER-role users to their Dealer record. Null for ADMIN/EMPLOYEE. */
    @Column(name = "dealer_id")
    private Long dealerId;
}