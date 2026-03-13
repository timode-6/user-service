package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Setter
public class UserDTO implements Serializable{
    
    private static final Long serialVersionUID = 1L;

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String surname;

    @Email
    private String email;

    private LocalDate birthDate;
    private boolean active;
}