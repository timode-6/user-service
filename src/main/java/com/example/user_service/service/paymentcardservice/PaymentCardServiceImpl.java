package com.example.user_service.service.paymentcardservice;

import com.example.user_service.model.User;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.exception.InactiveUserException;
import com.example.user_service.exception.PaymentCardNotFoundException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.mapper.PaymentCardMapper;
import com.example.user_service.repository.*;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
            .orElseThrow(() -> new InactiveUserException());
        
        PaymentCard paymentCard = PaymentCardMapper.INSTANCE.paymentCardDtoToPaymentCard(paymentCardDTO);
        paymentCard.setUser(user);
        return PaymentCardMapper.INSTANCE.paymentCardToPaymentCardDTO(paymentCardRepository.save(paymentCard));
    }

    @Override
    @Cacheable(value = CARD_CACHE, key = "#id", unless = "#result == null")
    public Optional<PaymentCardDTO> getPaymentCardById(Long id) {
        return paymentCardRepository.findById(id).map(PaymentCardMapper.INSTANCE::paymentCardToPaymentCardDTO);
    }

    @Override
    @Cacheable(value = CARD_CACHE, key = "#id", unless = "#result == null")
    public Set<PaymentCardDTO> getAllCardsByUserId(Long userId) {
        List<PaymentCard> paymentCards = paymentCardRepository.findByUserId(userId);
        return paymentCards.stream()
                       .map(PaymentCardMapper.INSTANCE::paymentCardToPaymentCardDTO) 
                       .collect(Collectors.toSet()); 
    }

    @Override
    @Transactional
    @CacheEvict(value = CARD_CACHE, key = "#id")
    public void deletePaymentCard(Long id){
        activateDeactivateCard(id, false);
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
        card.setActive(updatedCardDTO.isActive());
        return PaymentCardMapper.INSTANCE.paymentCardToPaymentCardDTO(paymentCardRepository.save(card));
    }

    @Override
    public void activateDeactivateCard(Long id, boolean active) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        card.setActive(active);
        paymentCardRepository.save(card);
    }
}
