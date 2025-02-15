package com.blooming.api.controller;

import com.blooming.api.entity.User;
import com.blooming.api.request.LogInRequest;
import com.blooming.api.response.LogInResponse;
import com.blooming.api.service.security.AuthService;
import com.blooming.api.service.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/logIn")
    public ResponseEntity<LogInResponse> authenticate(@Valid @RequestBody LogInRequest logInRequest) {
        User authenticatedUser = authService.authenticate(logInRequest.email(), logInRequest.password());
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LogInResponse logInResponse = LogInResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
        return ResponseEntity.ok(logInResponse);
    }

}
