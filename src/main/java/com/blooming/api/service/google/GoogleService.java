package com.blooming.api.service.google;

import com.blooming.api.entity.GoogleUser;
import com.blooming.api.entity.User;
import com.blooming.api.exception.GoogleTokenValidationException;
import com.blooming.api.repository.user.IUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for handling Google OAuth2 authentication and user registration.
 * This service validates Google tokens, registers new users, and authenticates existing users based on Google login.
 */
@Service
public class GoogleService implements IGoogleService {

    private final IUserRepository userRepository;

    private final RestTemplate googleRestTemplate;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.url}")
    private String googleClientUrl;

    /**
     * Constructor for GoogleService.
     *
     * @param userRepository     The repository for user data persistence.
     * @param googleRestTemplate The RestTemplate for making HTTP requests to Google's API.
     */
    public GoogleService(IUserRepository userRepository, RestTemplate googleRestTemplate) {
        this.userRepository = userRepository;
        this.googleRestTemplate = googleRestTemplate;
    }

    /**
     * Registers a new user or returns an existing user based on Google OAuth2 user information.
     *
     * @param oAuth2User The OAuth2User object containing the Google user's details.
     * @return The registered or existing user.
     */
    @Override
    public User registerOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String profileImageUrl = oAuth2User.getAttribute("picture");

        // Check if the user already exists in the repository
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            return existingUser;
        }

        // Create a new user if not found
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setProfileImageUrl(profileImageUrl);

        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user using their Google OAuth2 token.
     * The token is validated by sending it to Google's token info endpoint.
     *
     * @param googleToken The Google OAuth2 token.
     * @return The authenticated user.
     * @throws GoogleTokenValidationException If the token is invalid or the client ID does not match.
     */
    @Override
    public GoogleUser decryptGoogleToken(String googleToken) {
        // Send the token to Google's token info endpoint for validation
        String url = googleClientUrl + googleToken;

        ResponseEntity<GoogleUser> response = googleRestTemplate.getForEntity(url, GoogleUser.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            GoogleUser googleUserInfo = response.getBody();

            if (googleUserInfo == null || !googleClientId.equals(googleUserInfo.getAud())) {
                throw new GoogleTokenValidationException("Invalid Google token or mismatched client ID");
            }
            return googleUserInfo;

        } else {
            throw new GoogleTokenValidationException("Google token validation failed");
        }
    }
}
