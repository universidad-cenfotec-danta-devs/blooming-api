package com.blooming.api.controller;

import com.blooming.api.entity.Role;
import com.blooming.api.entity.GoogleUser;
import com.blooming.api.entity.RoleEnum;
import com.blooming.api.entity.User;
import com.blooming.api.request.LogInRequest;
import com.blooming.api.response.LogInResponse;
import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.service.google.IGoogleService;
import com.blooming.api.service.security.AuthService;
import com.blooming.api.service.security.JwtService;
import com.blooming.api.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for handling user authentication and login requests.
 * Provides endpoints for logging in with email/password or using Google OAuth2 authentication.
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final IGoogleService googleService;
    private final IUserService userService;

    /**
     * Constructor for AuthController.
     *
     * @param authService   The service for authenticating users with email/password.
     * @param jwtService    The service for generating and validating JWT tokens.
     * @param googleService The service for handling Google OAuth2 authentication.
     */
    public AuthController(AuthService authService, JwtService jwtService, IGoogleService googleService, IUserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.googleService = googleService;
        this.userService = userService;
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
        return generateLogInResponse(authenticatedUser);
    }

    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(request.getHeader("Authorization").replaceAll("Bearer ", ""));
        User user = userService.findByEmail(userEmail).
                orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
        return new GlobalHandlerResponse().handleResponse(
                HttpStatus.OK.name(),
                user,
                HttpStatus.OK, request);
    }

    /**
     * Authenticates a user via Google OAuth2. If the user already exists in the system, it logs them in.
     * If the user does not exist, it creates a new user with the information provided by Google and then logs them in.
     *
     * <p>This method decrypts the provided Google token, retrieves user information from Google, checks if the user exists
     * in the system, and either authenticates the existing user or registers a new user before authenticating them.</p>
     *
     * @param googleToken the Google token obtained from the front-end (must be provided as part of the URL).
     *                    This token is used to retrieve the user's information from Google.
     * @return a {@link ResponseEntity} containing a {@link LogInResponse} with the user's authentication status and token.
     * If successful, returns a 200 OK response with the JWT token.
     * If the user does not exist and the registration is successful, it returns a 200 OK response with the token.
     * @throws UsernameNotFoundException if the authentication process fails, i.e., if the user's credentials are invalid.
     */
    @PostMapping("/logInWithGoogle/{token}")
    public ResponseEntity<LogInResponse> authenticateWithGoogle(@PathVariable("token") String googleToken) {
        GoogleUser googleUser = googleService.decryptGoogleToken(googleToken);
        Optional<User> existingUserOpt = userService.findByEmail(googleUser.getEmail());

        String GOOGLE_DEFAULT_PASSWORD = "google_default_password";
        if (existingUserOpt.isEmpty()) {
            User user = new User();
            user.setName(googleUser.getName());
            user.setGoogleId(googleUser.getSub());
            user.setEmail(googleUser.getEmail());
            user.setPassword(GOOGLE_DEFAULT_PASSWORD);
            user.setProfileImageUrl(googleUser.getPicture());
            try {
                userService.register(user, RoleEnum.SIMPLE_USER);
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }

        User authenticatedUser = authService.authenticate(googleUser.getEmail(), GOOGLE_DEFAULT_PASSWORD);
        return generateLogInResponse(authenticatedUser);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Role role = user.getRole();
        return userService.register(user, role.getName());
    }

    /**
     * Helper method to generate the login response with JWT token and expiration time.
     *
     * @param user The authenticated user.
     * @return A ResponseEntity containing the login response.
     */
    private ResponseEntity<LogInResponse> generateLogInResponse(User user) {
        String jwtToken = jwtService.generateToken(user);
        LogInResponse logInResponse = LogInResponse.builder()
                .success(true)
                .token(jwtToken)
                .authUser(user)
                .expiresIn(jwtService.getExpirationTime())
                .build();
        return ResponseEntity.ok(logInResponse);
    }
}