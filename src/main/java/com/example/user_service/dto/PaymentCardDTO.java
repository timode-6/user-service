package com.example.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Setter;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardDTO implements Serializable{
    
    private static final Long serialVersionUID = 2L;

    private Long id;

    @NotBlank
    @Size(max = 20)
    private String number;

    @NotBlank
    @Size(max = 100)
    private String holder;

    private LocalDate expirationDate;
    private boolean active;
    
}
