package com.example.user_service.repository;

import com.example.user_service.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    List<PaymentCard> findByUserId(Long userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.holder = ?1")
    List<PaymentCard> findCardsByHolder(String holder);
}