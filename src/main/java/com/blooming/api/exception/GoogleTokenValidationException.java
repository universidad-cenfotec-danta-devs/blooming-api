package com.blooming.api.exception;

public class GoogleTokenValidationException extends RuntimeException {

    public GoogleTokenValidationException(String message) {
        super(message);
    }
}
