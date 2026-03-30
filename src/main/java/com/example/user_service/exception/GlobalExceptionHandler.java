package com.example.user_service.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map; 

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserLimitExceededException.class)
    public ResponseEntity<String> handleUserLimitExceeded(UserLimitExceededException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing 
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(UserUpdateFailedException.class)       
    public ResponseEntity<String> handleUserUpdateFailed(UserUpdateFailedException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)  
                .body(ex.getMessage());
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<String> handleInactiveUser(InactiveUserException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentCardNotFoundException.class)
    public ResponseEntity<String> handlePaymentCardNotFound(PaymentCardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
