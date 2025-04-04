package com.blooming.api.config;

import com.blooming.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 * This class defines route protection, session management, and integration of authentication and authorization filters.
 * It enables JWT and Google OAuth2 authentication, configuring both public and private routes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String ADMIN_USER = "ADMIN_USER";
    private final String DESIGNER_USER = "DESIGNER_USER";
    private final String SIMPLE_USER = "SIMPLE_USER";
    private final String NURSERY_USER = "NURSERY_USER";

    private final String LOG_IN_SIGN_IN_URI = "/api/users/**";
    private final String BASE_URI_PLANT = "/api/plant";
    private final String BASE_URI_PLANT_AI = "/api/plantAI";
    private final String BASE_URI_WATERING_PLAN = "/api/wateringPlan";

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor for SecurityConfig.
     *
     * @param authenticationProvider    The authentication provider that handles the authentication process.
     * @param jwtAuthenticationFilter   The filter that manages JWT-based authentication and validation.
    */
    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain, including route protection, authentication, and session management.
     *
     * @param httpSecurity The HttpSecurity object to configure security settings.
     * @return A configured SecurityFilterChain instance.
     * @throws Exception if an error occurs during security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.POST, LOG_IN_SIGN_IN_URI).permitAll()
                        .requestMatchers(HttpMethod.GET, "api/nurseries/actives").permitAll()
                        .requestMatchers(HttpMethod.GET, "api/nurseries/{id}").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return httpSecurity.build();
    }
}
