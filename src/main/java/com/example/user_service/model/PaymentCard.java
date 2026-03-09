package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import java.time.LocalDateTime;



@Entity
@Table(name = "payment_cards")
@Setter@Getter
public class PaymentCard extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String number;
    private String holder;
    private LocalDateTime expirationDate;
    private boolean active;

}