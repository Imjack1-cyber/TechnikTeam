// src/main/java/de/technikteam/filter/ApiAuthFilter.java
package de.technikteam.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class ApiAuthFilter implements Filter {

	private final AuthService authService;

	@Inject
	public ApiAuthFilter(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String authHeader = httpRequest.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
			return;
		}

		String token = authHeader.substring(7);
		User user = authService.validateTokenAndGetUser(token);

		if (user == null) {
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
			return;
		}

		// The AuthenticatedUserRequest wrapper is now the sole carrier of the user
		// principal.
		// Setting the attribute on the original request is no longer necessary and
		// could be unreliable.
		chain.doFilter(new AuthenticatedUserRequest(httpRequest, user), response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// No-op
	}

	@Override
	public void destroy() {
		// No-op
	}
}