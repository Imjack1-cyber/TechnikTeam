package de.technikteam.filter;

import de.technikteam.model.User; // CORRECTED: Added User import for clarity
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class.getName());

	private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList("/login", "/logout", "/calendar.ics"));

	private static final Set<String> PUBLIC_RESOURCE_PREFIXES = new HashSet<>(
			Arrays.asList("/css", "/js", "/images", "/error", "/public", "/vendor", "/wopi", "/api/auth"));

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AuthenticationFilter initialized by Guice.");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);

		String contextPath = request.getContextPath();
		String requestUri = request.getRequestURI();

		String path = requestUri.substring(contextPath.length());
		int semicolonIndex = path.indexOf(';');
		if (semicolonIndex != -1) {
			path = path.substring(0, semicolonIndex);
		}

		logger.trace("AuthenticationFilter processing request for path: '{}'", path);

		boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

		final String finalPath = path;
		boolean isPublicResource = PUBLIC_PATHS.contains(finalPath)
				|| PUBLIC_RESOURCE_PREFIXES.stream().anyMatch(prefix -> finalPath.startsWith(prefix));

		if (isLoggedIn || isPublicResource) {
			logger.trace("Access granted for path '{}'. LoggedIn: {}, IsPublic: {}", finalPath, isLoggedIn,
					isPublicResource);
			chain.doFilter(request, response);
		} else {
			logger.warn("Unauthorized access attempt by a guest to protected path: '{}'. Redirecting to login page.",
					finalPath);
			response.sendRedirect(contextPath + "/login");
		}
	}

	@Override
	public void destroy() {
		logger.info("AuthenticationFilter destroyed.");
	}
}