package com.example.user_service.exception;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException() {
        super("Cannot create payment card for inactive user!");
    }
}