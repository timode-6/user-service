package com.example.user_service.service.paymentcardservice;


import com.example.user_service.dto.PaymentCardDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.Optional;

public interface PaymentCardService {

    PaymentCardDTO createPaymentCard(Long userId, PaymentCardDTO paymentCardDto);

    Optional<PaymentCardDTO> getPaymentCardById(Long id);

    Set<PaymentCardDTO> getAllCardsByUserId(Long userId);

    @Transactional
    void deletePaymentCard(Long id);

    @Transactional
    PaymentCardDTO updatePaymentCard(Long id, PaymentCardDTO updateCardDto);

    void activateDeactivateCard(Long id, boolean active);

}
