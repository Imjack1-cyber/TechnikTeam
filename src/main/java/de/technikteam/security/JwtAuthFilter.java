package de.technikteam.security;

import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import de.technikteam.service.SystemSettingsService;
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

		// 1. Try to get token from Authorization header (for mobile clients)
		String token = null;
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
		}

		// 2. If no header, fall back to cookie (for web app)
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
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
					userDetails.getAuthorities());
			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}

		// 3. Maintenance Mode Check
		if (settingsService.isMaintenanceModeEnabled() && userDetails instanceof SecurityUser) {
			User currentUser = ((SecurityUser) userDetails).getUser();
			if (!currentUser.hasAdminAccess() && !MAINTENANCE_WHITELIST.contains(request.getRequestURI())) {
				response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
				response.setContentType("application/json");
				response.getWriter().write(
						"{\"success\":false,\"message\":\"Die Anwendung befindet sich derzeit im Wartungsmodus.\",\"data\":null}");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}