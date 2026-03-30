package com.example.user_service.exception;

public class UserLimitExceededException extends RuntimeException{
    public UserLimitExceededException() {
        super("User cannot have more than 5 cards");
    }
}