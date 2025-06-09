package de.technikteam.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.User;

// KEINE @WebFilter Annotation mehr, Konfiguration erfolgt in web.xml
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AdminFilter initialized.");
	}

	/**
	 * This filter protects all resources under the /admin/ path. It assumes the
	 * AuthenticationFilter has already run and a user is present in the session. It
	 * checks if the logged-in user has the 'ADMIN' role.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);

		// At this point, the AuthenticationFilter has already ensured a session exists.
		User user = (User) session.getAttribute("user");

		String path = request.getRequestURI().substring(request.getContextPath().length());
		logger.debug("AdminFilter processing request for path: '{}' for user: '{}'", path, user.getUsername());

		// Use equalsIgnoreCase for a more robust role check.
		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			// User is an admin. Allow access.
			logger.info("ADMIN access GRANTED for user '{}' to path '{}'. Passing to next filter/servlet.",
					user.getUsername(), path);
			chain.doFilter(request, response);
		} else {
			// User is logged in but is NOT an admin. Deny access.
			logger.warn("ADMIN access DENIED for user '{}' (Role: '{}') to path '{}'. Redirecting to user home.",
					user.getUsername(), user.getRole(), path);

			// Set a friendly error message for the user.
			request.getSession().setAttribute("accessErrorMessage",
					"Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");
			response.sendRedirect(request.getContextPath() + "/home");
		}
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}