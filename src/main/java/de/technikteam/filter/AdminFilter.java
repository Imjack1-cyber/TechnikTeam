// src/main/java/de/technikteam/filter/AdminFilter.java
package de.technikteam.filter;

import de.technikteam.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AdminFilter initialized. This filter checks for administrative permissions.");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String path = request.getRequestURI().substring(request.getContextPath().length());

		User user = (User) request.getAttribute("user");

		if (user == null) {
			logger.error(
					"AdminFilter Error: User object not found in request attribute for protected admin path '{}'. This indicates a failure in the upstream authentication filter.",
					path);
			// A missing user principal is an authentication failure, not authorization.
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication context is missing.");
			return;
		}

		if (user.hasAdminAccess()) {
			logger.trace("ADMIN area access GRANTED for user '{}' to path '{}'.", user.getUsername(), path);
			chain.doFilter(request, response);
		} else {
			logger.warn(
					"ADMIN access DENIED for user '{}' (Permissions: {}) to path '{}'. No relevant admin permissions found.",
					user.getUsername(), user.getPermissions(), path);
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You do not have permission to access this administrative area.");
		}
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}