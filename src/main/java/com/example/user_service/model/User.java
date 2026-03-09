package com.example.user_service.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Setter;
import lombok.Getter;
import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Setter@Getter
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String surname;
    private LocalDateTime birthDate;
    private String email;
    private boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<PaymentCard> paymentCards;
}