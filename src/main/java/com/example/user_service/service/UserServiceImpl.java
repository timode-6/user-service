package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.mapper.PaymentCardMapper;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.repository.*;
import com.example.user_service.spec.UserSpecification;

import lombok.AllArgsConstructor;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.mapper.PaymentCardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;


import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;


@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    public static final String USER_CACHE = "users";
    public static final String CARD_CACHE = "cards";


    @Override
    @CachePut(value = USER_CACHE, key = "#result.id")
    public UserDTO createUser(UserDTO userDTO){
        User user = UserMapper.INSTANCE.userDtoToUser(userDTO);
        return UserMapper.INSTANCE.userToUserDTO(userRepository.save(user));
    }

    @Override
    @CachePut(value = CARD_CACHE, key = "#result.id")
    public PaymentCardDTO createPaymentCard(Long userId, PaymentCardDTO paymentCardDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    
        PaymentCard paymentCard = PaymentCardMapper.INSTANCE.paymentCardDtoToPaymentCard(paymentCardDTO);
        paymentCard.setUser(user);
        return PaymentCardMapper.INSTANCE.paymentCardToPaymentCardDTO(paymentCardRepository.save(paymentCard));
    }

    @Override
    @Cacheable(value = USER_CACHE, key = "#id")
    public Optional<UserDTO> getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.map(UserMapper.INSTANCE::userToUserDTO);
    }

    @Override
    @Cacheable(value = CARD_CACHE, key = "#id")
    public Optional<PaymentCardDTO> getPaymentCardById(Long id) {
        Optional<PaymentCard> optionalPaymentCard = paymentCardRepository.findById(id);
        return optionalPaymentCard.map(PaymentCardMapper.INSTANCE::paymentCardToPaymentCardDTO);
    }

    @Override
    public Page<UserDTO> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = (root, query, builder) -> null;
        if (firstName != null) {
            spec = spec.and(UserSpecification.hasFirstName(firstName));
        }
        if (surname != null) {
            spec = spec.and(UserSpecification.hasSurname(surname));
        }

        Page<User> usersPage = userRepository.findAll(spec, pageable);
    
        return usersPage.map(UserMapper.INSTANCE::userToUserDTO);
    }

    @Override
    public Set<PaymentCardDTO> getAllCardsByUserId(Long userId) {
         List<PaymentCard> paymentCards = paymentCardRepository.findByUserId(userId);
        return paymentCards.stream()
                       .map(PaymentCardMapper.INSTANCE::paymentCardToPaymentCardDTO) 
                       .collect(Collectors.toSet()); 
    }

    @Override
    @Transactional
    @CachePut(value = USER_CACHE, key = "#result.id")
    public UserDTO updateUser(Long id, UserDTO updatedUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(updatedUserDTO.getName());
        user.setSurname(updatedUserDTO.getSurname());
        user.setBirthDate(updatedUserDTO.getBirthDate());
        user.setEmail(updatedUserDTO.getEmail());
        user.setActive(updatedUserDTO.isActive());
        return UserMapper.INSTANCE.userToUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_CACHE, key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = CARD_CACHE, key = "#id")
    public void deletePaymentCard(Long id){
        paymentCardRepository.deleteById(id);
    }

    @Override
    @CachePut(value = CARD_CACHE, key = "#result.id")
    public PaymentCardDTO updatePaymentCard(Long id, PaymentCardDTO updatedCardDTO) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setNumber(updatedCardDTO.getNumber());
        card.setHolder(updatedCardDTO.getHolder());
        card.setExpirationDate(updatedCardDTO.getExpirationDate());
        card.setActive(updatedCardDTO.isActive());
        return PaymentCardMapper.INSTANCE.paymentCardToPaymentCardDTO(paymentCardRepository.save(card));
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
