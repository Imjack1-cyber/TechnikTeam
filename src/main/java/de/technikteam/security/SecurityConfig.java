package de.technikteam.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central configuration for Spring Security. This class defines the
 * application's security policies, such as which endpoints are public and which
 * require authentication, and integrates the custom JWT filter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF protection, as it's not needed for a stateless JWT-based API
				.csrf(csrf -> csrf.disable())
				// Configure authorization rules for different endpoints
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**", // Authentication endpoints
						"/swagger-ui.html", // Swagger UI main page
						"/swagger-ui/**", // Swagger UI resources
						"/v3/api-docs/**" // OpenAPI specification
				).permitAll().anyRequest().authenticated() // All other requests must be authenticated
				)
				// Configure session management to be stateless
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Add the custom JWT filter before the standard authentication filter
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}