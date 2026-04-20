package com.example.user_service.service.paymentcardservice;

import com.example.user_service.model.User;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.exception.InactiveUserException;
import com.example.user_service.exception.PaymentCardNotFoundException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.exception.AccessDeniedException;
import com.example.user_service.mapper.PaymentCardMapper;
import com.example.user_service.repository.*;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService{
    
    private UserRepository userRepository;

    private PaymentCardRepository paymentCardRepository;

    public static final String CARD_CACHE = "cards";

    @Override
    @CachePut(value = CARD_CACHE, key = "#result.id")
    public PaymentCardDTO createPaymentCard(Long userId, PaymentCardDTO paymentCardDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        user = Optional.of(user).filter(User::isActive)
            .orElseThrow(InactiveUserException::new);
        
        PaymentCard paymentCard = PaymentCardMapper.INSTANCE.paymentCardDtoToPaymentCard(paymentCardDTO);
        paymentCard.setUser(user);
        return PaymentCardMapper.INSTANCE.paymentCardToPaymentCardDTO(paymentCardRepository.save(paymentCard));
    }

    @Override
    @Cacheable(value = CARD_CACHE, key = "#cardId", unless = "#result == null")
    public Optional<PaymentCardDTO> getPaymentCardById(Long userId, Long cardId) {
        return paymentCardRepository.findById(cardId)
                .filter(card -> card.getUser().getId().equals(userId))  
                .map(PaymentCardMapper.INSTANCE::paymentCardToPaymentCardDTO);
    }

    @Override
    @Cacheable(value = CARD_CACHE, key = "#userId", unless = "#result == null")
    public Set<PaymentCardDTO> getAllCardsByUserId(Long userId) {
        List<PaymentCard> paymentCards = paymentCardRepository.findByUserId(userId);
        return paymentCards.stream()
                       .map(PaymentCardMapper.INSTANCE::paymentCardToPaymentCardDTO) 
                       .collect(Collectors.toSet()); 
    }

    @Override
    @Transactional
    @CacheEvict(value = CARD_CACHE, key = "#cardId")
    public void deletePaymentCard(Long userId, Long cardId) {
       PaymentCard card = verifyCardOwnership(userId, cardId);
        card.setActive(false);
        paymentCardRepository.save(card);
    }

    @Override
    @Transactional
    @CachePut(value = CARD_CACHE, key = "#result.id")
    public PaymentCardDTO updatePaymentCard(Long id, PaymentCardDTO updatedCardDTO) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        card.setNumber(updatedCardDTO.getNumber());
        card.setHolder(updatedCardDTO.getHolder());
        card.setExpirationDate(updatedCardDTO.getExpirationDate());
        return PaymentCardMapper.INSTANCE.paymentCardToPaymentCardDTO(paymentCardRepository.save(card));
    }

    @Override
    @Transactional
    public void activateDeactivateCard(Long userId, Long cardId, boolean active) {
        PaymentCard card = verifyCardOwnership(userId, cardId);
        card.setActive(active);
        paymentCardRepository.save(card);
    }

    

    private PaymentCard verifyCardOwnership(Long userId, Long cardId) {
        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new PaymentCardNotFoundException(cardId));
        if (!card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Card does not belong to user with id:" + userId);
        }
        return card;
    }
}
