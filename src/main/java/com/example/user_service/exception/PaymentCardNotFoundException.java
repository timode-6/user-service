package com.example.user_service.exception;

public class PaymentCardNotFoundException extends RuntimeException{
    public PaymentCardNotFoundException(Long id) {
        super("Payment card not found: " + id);
    }
}
