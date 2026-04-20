package com.example.user_service.controller;

import com.example.user_service.exception.PaymentCardNotFoundException;
import com.example.user_service.service.paymentcardservice.PaymentCardService;
import com.example.user_service.dto.PaymentCardDTO;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/card")
@AllArgsConstructor
public class PaymentCardController {

    private PaymentCardService paymentCardService;

    @PostMapping("/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.principal")
    public ResponseEntity<PaymentCardDTO> createPaymentCard(@PathVariable Long userId, 
                                        @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        return new ResponseEntity<>(paymentCardService.createPaymentCard(userId, paymentCardDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/cards/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.principal")
    public ResponseEntity<PaymentCardDTO> getCardById(@PathVariable Long userId, @PathVariable Long cardId) {
        Optional<PaymentCardDTO> paymentCardDTO = paymentCardService.getPaymentCardById(userId, cardId);
        return paymentCardDTO.map(ResponseEntity::ok)
                            .orElseThrow(() -> new PaymentCardNotFoundException(cardId));
    }

    @GetMapping("/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.principal")
    public ResponseEntity<Set<PaymentCardDTO>> getAllCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getAllCardsByUserId(userId));
    }

    @PutMapping("/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.principal")
    public ResponseEntity<PaymentCardDTO> updatePaymentCard(@PathVariable Long userId, 
                              @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        PaymentCardDTO updatedCard = paymentCardService.updatePaymentCard(userId, paymentCardDTO);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{userId}/cards/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.principal")
    public ResponseEntity<Void> deletePaymentCard(
            @PathVariable Long userId,
            @PathVariable Long cardId) {
        paymentCardService.deletePaymentCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/cards/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setCardStatus(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @RequestParam boolean active) {
        paymentCardService.activateDeactivateCard(userId, cardId, active);
        return ResponseEntity.noContent().build();
    }
}
