package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "payment_cards")
@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PaymentCard extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String number;
    private String holder;
    private LocalDate expirationDate;
    private boolean active;

}