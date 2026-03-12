package com.example.user_service.service;

import com.example.user_service.dto.UserDTO;

import com.example.user_service.dto.PaymentCardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface UserService {
    
    UserDTO createUser(UserDTO userDto);

    PaymentCardDTO createPaymentCard(Long userId, PaymentCardDTO paymentCardDto);

    Optional<UserDTO> getUserById(Long id);

    Optional<PaymentCardDTO> getPaymentCardById(Long id);

    Page<UserDTO> getAllUsers(String firstName, String surname, Pageable pageable);

    Set<PaymentCardDTO> getAllCardsByUserId(Long userId);

    @Transactional
    UserDTO updateUser(Long id, UserDTO updateUserDto);
    
    @Transactional
    void deleteUser(Long id);

    @Transactional
    void deletePaymentCard(Long id);

    PaymentCardDTO updatePaymentCard(Long id, PaymentCardDTO updateCardDto);
    
    void activateDeactivateUser(Long id, boolean active);

    void activateDeactivateCard(Long id, boolean active);
}
