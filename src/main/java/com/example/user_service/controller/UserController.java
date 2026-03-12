package com.example.user_service.controller;

import com.example.user_service.dto.UserDTO;
import com.example.user_service.exception.CardNotFoundException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.service.UserService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<UserDTO> userDTO = userService.getUserById(id);
        return userDTO.map(ResponseEntity::ok)
                      .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<PaymentCardDTO> createPaymentCard(@PathVariable Long userId, 
                                        @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        return new ResponseEntity<>(userService.createPaymentCard(userId, paymentCardDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/cards/{cardId}")
    public ResponseEntity<PaymentCardDTO> getCardById(@PathVariable Long userId, @PathVariable Long cardId) {
        Optional<PaymentCardDTO> paymentCardDTO = userService.getPaymentCardById(cardId);
        return paymentCardDTO.map(ResponseEntity::ok)
                            .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    @PutMapping("/cards/{id}")
    @Transactional
    public ResponseEntity<PaymentCardDTO> updatePaymentCard(@PathVariable Long id, 
                              @Valid @RequestBody PaymentCardDTO paymentCardDTO) {
        PaymentCardDTO updatedCard = userService.updatePaymentCard(id, paymentCardDTO);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/cards/{id}")
    @Transactional
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        userService.deletePaymentCard(id);
        return ResponseEntity.noContent().build();
    }
}