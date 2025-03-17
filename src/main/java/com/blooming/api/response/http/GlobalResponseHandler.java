package com.blooming.api.response.http;

import com.itextpdf.text.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalResponseHandler {
    @ResponseBody
    public <T> ResponseEntity<?> _handleResponse(String message, T body, HttpStatus status, HttpServletRequest request) {
        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        if (body instanceof HttpResponse) {
            HttpResponse<?> response = (HttpResponse<?>) body;
            response.setMeta(meta);
            return new ResponseEntity<>(response, status);
        }
        HttpResponse<T> response = new HttpResponse<>(message, body, meta);
        return  new ResponseEntity<>(response, status);
    }

    @ResponseBody
    public <T> ResponseEntity<?> _handleResponse(String message, HttpStatus status, HttpServletRequest request) {
        MetaResponse meta = new MetaResponse(request.getMethod(), request.getRequestURL().toString());
        HttpResponse<?> response = new HttpResponse<>(message, meta);
        return  new ResponseEntity<>(response, status);
    }

    @ResponseBody
    public <T> ResponseEntity<?> _handleResponse(String message, T body, HttpStatus status, MetaResponse meta) {
        if (body instanceof HttpResponse) {
            HttpResponse<?> response = (HttpResponse<?>) body;
            response.setMeta(meta);
            return new ResponseEntity<>(response, status);
        }
        HttpResponse<T> response = new HttpResponse<>(message, body, meta);
        return  new ResponseEntity<>(response, status);
    }
}