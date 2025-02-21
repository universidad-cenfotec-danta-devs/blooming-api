package com.blooming.api.response.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandlerResponse {

    @ResponseBody
    public <T> ResponseEntity<HttpResponse<T>> handleResponse(String message, T body, HttpStatus status, MetaResponse meta, HttpServletRequest request) {
        if (meta == null) {
            meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        }
        HttpResponse<T> response = new HttpResponse<>(message, body, meta);
        return new ResponseEntity<>(response, status);
    }

    @ResponseBody
    public <T> ResponseEntity<HttpResponse<T>> handleResponse(String message, T body, HttpStatus status, HttpServletRequest request) {
        return handleResponse(message, body, status, null, request);
    }

    @ResponseBody
    public <T> ResponseEntity<HttpResponse<T>> handleResponse(String message, HttpStatus status, HttpServletRequest request) {
        return handleResponse(message, null, status, request);
    }
}
