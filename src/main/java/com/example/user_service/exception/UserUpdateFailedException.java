package com.example.user_service.exception;

public class UserUpdateFailedException extends RuntimeException {
    public UserUpdateFailedException(Long id) {
        super("Failed to update: User with id " + id + " not found");
    }
}