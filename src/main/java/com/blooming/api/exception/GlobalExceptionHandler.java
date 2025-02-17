package com.blooming.api.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public <T> ResponseEntity<T> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("Entity not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public <T> ResponseEntity<T> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Invalid argument: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @ExceptionHandler(Exception.class)
    public <T> ResponseEntity<T> handleGlobalException(Exception ex) {
        logger.error("Internal server error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<Map<String, String>> handleJsonMappingException(JsonMappingException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Invalid request data. Please ensure all fields are correct and follow the expected format.");
        logger.error("Json mapping error: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseException(JsonParseException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Invalid request data. Please ensure all fields are correct and follow the expected format.");
        logger.error("Parsing error: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleExpiredJwtException(ExpiredJwtException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Token expired");
        logger.error("Expired JWT error: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
//        Map<String, String> errorDetails = new HashMap<>();
//        errorDetails.put("error", "Invalid Credentials");
//        logger.error("Invalid Credentials: {}", ex.getMessage());
//        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
//    }


}
