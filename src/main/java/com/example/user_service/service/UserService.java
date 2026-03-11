package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.model.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface UserService {
    User createUser(User user);

    PaymentCard createPaymentCard(Long userId, PaymentCard paymentCard);

    Optional<User> getUserById(Long id);

    Optional<PaymentCard> getPaymentCardById(Long id);

    Page<User> getAllUsers(String firstName, String surname, Pageable pageable);

    Set<PaymentCard> getAllCardsByUserId(Long userId);

    User updateUser(Long id, User updateUser);

    PaymentCard updatePaymentCard(Long id, PaymentCard updateCard);
    
    void activateDeactivateUser(Long id, boolean active);

    void activateDeactivateCard(Long id, boolean active);
}
