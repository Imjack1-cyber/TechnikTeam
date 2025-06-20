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

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

	// Add "/notifications" to the set of public paths.
	private static final Set<String> PUBLIC_PATHS = new HashSet<>(
			Arrays.asList("/login", "/login.jsp", "/error404.jsp", "/error500.jsp", "/notifications"));

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

		logger.debug("AuthenticationFilter processing request for path: '{}'", path);

		boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

		if (isLoggedIn) {
			chain.doFilter(request, response);
			return;
		}

		if (PUBLIC_PATHS.contains(path) || PUBLIC_RESOURCE_PREFIXES.stream().anyMatch(path::startsWith)) {
			chain.doFilter(request, response);
			return;
		}
		
		logger.warn("Unauthorized access attempt by a guest to protected path: '{}'. Redirecting to login page.", path);
		response.sendRedirect(contextPath + "/login");
	}

	@Override
	public void destroy() {
		logger.info("AuthenticationFilter destroyed.");
	}
}