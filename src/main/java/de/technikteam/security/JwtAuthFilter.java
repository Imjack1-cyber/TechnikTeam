package de.technikteam.security;

import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A Spring Security filter that intercepts all incoming requests to validate the JWT.
 * If the token is valid, it sets the authenticated user in the Spring Security context.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Autowired
    public JwtAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        User user = authService.validateTokenAndGetUser(token);

        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // If the token is valid and there's no existing authentication, create one.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null, // No credentials needed as we've validated the token
                null  // Authorities/roles can be added here if needed
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}