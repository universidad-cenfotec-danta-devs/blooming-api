package com.blooming.api.controller;

import com.blooming.api.entity.User;
import com.blooming.api.request.LogInRequest;
import com.blooming.api.response.LogInResponse;
import com.blooming.api.service.google.GoogleService;
import com.blooming.api.service.security.AuthService;
import com.blooming.api.service.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user authentication and login requests.
 * Provides endpoints for logging in with email/password or using Google OAuth2 authentication.
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final GoogleService googleService;

    /**
     * Constructor for AuthController.
     *
     * @param authService The service for authenticating users with email/password.
     * @param jwtService The service for generating and validating JWT tokens.
     * @param googleService The service for handling Google OAuth2 authentication.
     */
    public AuthController(AuthService authService, JwtService jwtService, GoogleService googleService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.googleService = googleService;
    }

    /**
     * Endpoint for user login with email and password.
     * Authenticates the user, generates a JWT token, and returns the token with expiration time.
     *
     * @param logInRequest The login request containing the user's email and password.
     * @return A response entity containing the JWT token and expiration time.
     */
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

    /**
     * Endpoint for user login using Google OAuth2 token.
     * Validates the Google token, retrieves the user's information, generates a JWT token,
     * and returns the token with expiration time.
     *
     * @param googleToken The Google OAuth2 token provided by the client.
     * @return A response entity containing the JWT token and expiration time.
     */
    @PostMapping("/logInWithGoogle")
    public ResponseEntity<LogInResponse> authenticateWithGoogle(@RequestParam("token") String googleToken) {
        User googleUser = googleService.authenticateWithGoogle(googleToken);  // Assumes GoogleService handles token validation
        String jwtToken = jwtService.generateToken(googleUser);
        LogInResponse logInResponse = LogInResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
        return ResponseEntity.ok(logInResponse);
    }

}
