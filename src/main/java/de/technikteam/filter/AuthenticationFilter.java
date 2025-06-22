package de.technikteam.filter;

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
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * A security filter that intercepts all incoming requests (`/*`) to enforce
 * authentication. It checks if a user is logged in by looking for a "user"
 * object in the session. It allows access to a predefined set of public pages
 * (like login, error pages) and resources (CSS, JS) for everyone. For all other
 * protected paths, it redirects unauthenticated users to the login page.
 */
@WebFilter(value = "/*", asyncSupported = true)
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class.getName());

	// A set of paths that are publicly accessible without logging in.
	private static final Set<String> PUBLIC_PATHS = new HashSet<>(
			Arrays.asList("/login", "/login.jsp", "/logout.jsp", "/error404.jsp", "/error500.jsp"));

	// A set of resource prefixes that are always public (e.g., /css/style.css).
	private static final Set<String> PUBLIC_RESOURCE_PREFIXES = new HashSet<>(Arrays.asList("/css", "/js", "/images"));

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AuthenticationFilter initialized. Public paths: {}, Public prefixes: {}", PUBLIC_PATHS,
				PUBLIC_RESOURCE_PREFIXES);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);

		String contextPath = request.getContextPath();
		String path = request.getRequestURI().substring(contextPath.length());

		logger.trace("AuthenticationFilter processing request for path: '{}'", path);

		boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

		boolean isPublicResource = PUBLIC_PATHS.contains(path)
				|| PUBLIC_RESOURCE_PREFIXES.stream().anyMatch(path::startsWith);

		// If the user is logged in OR the path is public, let the request through.
		if (isLoggedIn || isPublicResource) {
			logger.trace("Access granted for path '{}'. LoggedIn: {}, IsPublic: {}", path, isLoggedIn,
					isPublicResource);
			chain.doFilter(request, response);
		} else {
			// If not logged in and not a public path, redirect to login.
			logger.warn("Unauthorized access attempt by a guest to protected path: '{}'. Redirecting to login page.",
					path);
			response.sendRedirect(contextPath + "/login");
		}
	}

	@Override
	public void destroy() {
		logger.info("AuthenticationFilter destroyed.");
	}
}