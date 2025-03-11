package com.blooming.api.service.security;

import com.blooming.api.service.google.GoogleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles the success of Google OAuth2 authentication.
 * This handler is triggered after successful authentication and ensures that the user is registered or updated in the system.
 * It delegates the user registration or update logic to the {@link GoogleService}.
 */
@Component
public class GoogleLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final GoogleService googleService;

    /**
     * Constructor for GoogleLoginSuccessHandler.
     *
     * @param googleService The service for handling Google OAuth2 user registration and updates.
     */
    public GoogleLoginSuccessHandler(GoogleService googleService) {
        this.googleService = googleService;
    }

    /**
     * Called upon successful authentication to handle user registration or updating.
     * It registers the user if they are new or updates their profile information if they already exist.
     *
     * @param request        The HTTP request.
     * @param response       The HTTP response.
     * @param authentication The authentication object containing the authenticated user's details.
     * @throws ServletException If an error occurs during the processing of the request.
     * @throws IOException      If an I/O error occurs during the processing of the request.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        googleService.registerOAuth2User(oAuth2User);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
