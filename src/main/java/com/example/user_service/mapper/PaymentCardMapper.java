package com.example.user_service.mapper;

import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentCardMapper {
    
    PaymentCardMapper INSTANCE = Mappers.getMapper(PaymentCardMapper.class);

    PaymentCardDTO paymentCardToPaymentCardDTO(PaymentCard paymentCard);
    PaymentCard paymentCardDtoToPaymentCard(PaymentCardDTO paymentCardDTO);
}
