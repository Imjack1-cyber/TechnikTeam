package de.technikteam.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.technikteam.model.User;
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
 * A security filter that protects all URLs under the `/admin/*` path. It
 * intercepts requests to these protected endpoints and checks if the user
 * object in the current session has the "ADMIN" role. It provides special
 * access for the 'LAGERWART' role to specific pages.
 */
@WebFilter(urlPatterns = "/admin/*", asyncSupported = true)
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);
	private static final List<String> LAGERWART_PATHS = Arrays.asList("/admin/storage", "/admin/defects");

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AdminFilter initialized and protecting /admin/* paths.");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);

		String path = request.getRequestURI().substring(request.getContextPath().length());

		logger.trace("AdminFilter is processing request for path: '{}'", path);

		// Check if the session exists AND if the user object is in the session.
		if (session == null || session.getAttribute("user") == null) {
			logger.warn("Admin access DENIED to path '{}'. No active session found. Redirecting to login.", path);
			response.sendRedirect(request.getContextPath() + "/login");
			return; // Stop processing this request.
		}

		User user = (User) session.getAttribute("user");
		String userRole = user.getRole();

		// Admins can access everything.
		if ("ADMIN".equalsIgnoreCase(userRole)) {
			logger.debug("ADMIN access GRANTED for user '{}' to path '{}'. Passing to next filter/servlet.",
					user.getUsername(), path);
			chain.doFilter(request, response);
			return;
		}

		// Check for Lagerwart specific access.
		if ("LAGERWART".equalsIgnoreCase(userRole)) {
			for (String allowedPath : LAGERWART_PATHS) {
				if (path.startsWith(allowedPath)) {
					logger.debug("LAGERWART access GRANTED for user '{}' to path '{}'.", user.getUsername(), path);
					chain.doFilter(request, response);
					return;
				}
			}
		}

		// If we reach here, the user is neither an Admin nor a Lagerwart with access to
		// a permitted page.
		logger.warn("ADMIN access DENIED for user '{}' (Role: '{}') to path '{}'. Redirecting to user home.",
				user.getUsername(), user.getRole(), path);
		request.getSession().setAttribute("accessErrorMessage",
				"Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");
		response.sendRedirect(request.getContextPath() + "/home");
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}