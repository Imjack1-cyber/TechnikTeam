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

@WebFilter(value = "/*", asyncSupported = true)
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class.getName());

	// CORRECTION: This set should only contain the servlet paths, not JSP paths.
	// ADDED: /pack-kit to allow access from QR codes.
	private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList("/login", "/logout", "/pack-kit"));

	// CORRECTION: The /error directory prefix is correct for allowing error pages.
	// ADDED: /public to allow access to public APIs like the iCal feed.
	private static final Set<String> PUBLIC_RESOURCE_PREFIXES = new HashSet<>(
			Arrays.asList("/css", "/js", "/images", "/error", "/public"));

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

		if (isLoggedIn || isPublicResource) {
			logger.trace("Access granted for path '{}'. LoggedIn: {}, IsPublic: {}", path, isLoggedIn,
					isPublicResource);
			chain.doFilter(request, response);
		} else {
			logger.warn("Unauthorized access attempt by a guest to protected path: '{}'. Redirecting to login page.",
					path);
			// CORRECTION: Redirect to the /login servlet URL, not the JSP file path.
			response.sendRedirect(contextPath + "/login");
		}
	}

	@Override
	public void destroy() {
		logger.info("AuthenticationFilter destroyed.");
	}
}