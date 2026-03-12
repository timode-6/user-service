package com.example.user_service.exception;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException(Long id){
        super("Card not found with ID: " + id);
    }
}