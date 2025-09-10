package de.technikteam.security;

import de.technikteam.api.v1.dto.MaintenanceStatusDTO;
import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import de.technikteam.service.SystemSettingsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final AuthService authService;
	private final SystemSettingsService settingsService;

	private static final List<String> MAINTENANCE_WHITELIST = List.of("/api/v1/auth/me", "/api/v1/auth/logout",
			"/api/v1/admin/system/maintenance");

	@Autowired
	public JwtAuthFilter(AuthService authService, SystemSettingsService settingsService) {
		this.authService = authService;
		this.settingsService = settingsService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestUri = request.getRequestURI();
        final String contextPath = request.getContextPath();

        String token = null;

        // 1. Prioritize Authorization header (for native clients and SSE/WS query params)
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (requestUri.contains("/sse") || requestUri.contains("/ws/")) {
            token = request.getParameter("token");
        }
        
        // 2. If no header or query param, fall back to cookie (for web app)
        if (token == null && request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> AuthService.AUTH_COOKIE_NAME.equals(cookie.getName())).map(Cookie::getValue)
                    .findFirst().orElse(null);
        }

		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		UserDetails userDetails = authService.validateTokenAndGetUser(token);

		if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// Add JTI to the user object in the security context
			if (userDetails instanceof SecurityUser) {
				Claims claims = authService.parseTokenClaims(token);
				((SecurityUser) userDetails).getUser().setJti(claims.getId());
			}
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
					userDetails.getAuthorities());
			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}

		// 4. Maintenance Mode Check
		if (userDetails instanceof SecurityUser) {
			MaintenanceStatusDTO status = settingsService.getMaintenanceStatus();
			User currentUser = ((SecurityUser) userDetails).getUser();

			if ("HARD".equals(status.mode()) && !currentUser.hasAdminAccess() && !isMaintenanceWhitelisted(requestUri, contextPath)) {
				response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
				response.setContentType("application/json");
				response.getWriter().write(
						"{\"success\":false,\"message\":\"" + status.message() + "\",\"data\":null}");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
    
    private boolean isMaintenanceWhitelisted(String requestUri, String contextPath) {
        return MAINTENANCE_WHITELIST.stream()
                .anyMatch(whitelistedPath -> requestUri.equals(contextPath + whitelistedPath));
    }
}