package com.example.user_service.service;

import com.example.user_service.model.*;
import com.example.user_service.repository.*;
import com.example.user_service.spec.UserSpecification;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Override
    public User createUser(User user){
        return userRepository.save(user);
    }

    @Override
    public PaymentCard createPaymentCard(Long userId, PaymentCard paymentCard) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getPaymentCards().size() >= 5) {
            throw new RuntimeException("User cannot have more than 5 cards");
        }
        paymentCard.setUser(user);
        return paymentCardRepository.save(paymentCard);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<PaymentCard> getPaymentCardById(Long id) {
        return paymentCardRepository.findById(id);
    }

    @Override
    public Page<User> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = (root, query, builder) -> null;
        if (firstName != null) {
            spec = spec.and(UserSpecification.hasFirstName(firstName));
        }
        if (surname != null) {
            spec = spec.and(UserSpecification.hasSurname(surname));
        }
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public Set<PaymentCard> getAllCardsByUserId(Long userId) {
        return (Set<PaymentCard>) paymentCardRepository.findByUserId(userId);
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(updatedUser.getName());
        user.setSurname(updatedUser.getSurname());
        user.setBirthDate(updatedUser.getBirthDate());
        user.setEmail(updatedUser.getEmail());
        user.setActive(updatedUser.isActive());
        return userRepository.save(user);
    }

    @Override
    public PaymentCard updatePaymentCard(Long id, PaymentCard updatedCard) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setNumber(updatedCard.getNumber());
        card.setHolder(updatedCard.getHolder());
        card.setExpirationDate(updatedCard.getExpirationDate());
        card.setActive(updatedCard.isActive());
        return paymentCardRepository.save(card);
    }

    @Override
    public void activateDeactivateUser(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    public void activateDeactivateCard(Long id, boolean active) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setActive(active);
        paymentCardRepository.save(card);
    }
}
