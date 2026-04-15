package com.example.user_service.controller;

import com.example.user_service.exception.PaymentCardNotFoundException;
import com.example.user_service.service.paymentcardservice.PaymentCardService;
import com.example.user_service.dto.PaymentCardDTO;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/card")
@AllArgsConstructor
public class PaymentCardController {

    private PaymentCardService paymentCardService;

    @PostMapping("/{userId}/cards")
    public ResponseEntity<PaymentCardDTO> createPaymentCard(@PathVariable Long userId, 
                                        @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        return new ResponseEntity<>(paymentCardService.createPaymentCard(userId, paymentCardDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/cards/{cardId}")
    public ResponseEntity<PaymentCardDTO> getCardById(@PathVariable Long userId, @PathVariable Long cardId) {
        Optional<PaymentCardDTO> paymentCardDTO = paymentCardService.getPaymentCardById(cardId);
        return paymentCardDTO.map(ResponseEntity::ok)
                            .orElseThrow(() -> new PaymentCardNotFoundException(cardId));
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<PaymentCardDTO> updatePaymentCard(@PathVariable Long id, 
                              @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        PaymentCardDTO updatedCard = paymentCardService.updatePaymentCard(id, paymentCardDTO);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        paymentCardService.deletePaymentCard(id);
        return ResponseEntity.noContent().build();
    }
}
