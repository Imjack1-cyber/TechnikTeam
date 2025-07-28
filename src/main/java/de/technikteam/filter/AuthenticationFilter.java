// src/main/java/de/technikteam/filter/AuthenticationFilter.java
package de.technikteam.filter;

import de.technikteam.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A legacy filter for handling session-based authentication. This is now ONLY
 * used for non-React pages/servlets like /notifications. It must ignore API
 * paths meant for JWT auth.
 */
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

	private Set<String> publicPaths;
	private static final Set<String> IGNORED_PREFIXES = new HashSet<>(Arrays.asList("/api/v1/"));

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("Session-based AuthenticationFilter initialized.");
		String contextPath = filterConfig.getServletContext().getContextPath();
		publicPaths = new HashSet<>(Arrays.asList(contextPath + "/login", contextPath + "/logout"));
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		String path = request.getRequestURI();

		// Let the request pass if it's an API path meant for JWT auth or a public
		// resource path.
		if (IGNORED_PREFIXES.stream().anyMatch(p -> path.startsWith(request.getContextPath() + p))) {
			chain.doFilter(req, res);
			return;
		}

		// For all other paths (e.g., /notifications), check for a session.
		HttpSession session = request.getSession(false);
		User user = (session != null) ? (User) session.getAttribute("user") : null;

		if (user != null) {
			request.setAttribute("user", user);
			chain.doFilter(req, res);
		} else {
			// If it's not an ignored path and there's no session, it's unauthorized.
			logger.warn("Session-based authentication failed for path: {}. No user in session.", path);
			((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session not authenticated.");
		}
	}

	@Override
	public void destroy() {
	}
}