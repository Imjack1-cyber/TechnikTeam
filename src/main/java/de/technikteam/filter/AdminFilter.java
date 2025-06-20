package de.technikteam.filter;

import java.io.IOException;
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

@WebFilter(urlPatterns = "/admin/*", asyncSupported = true)
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AdminFilter initialized.");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);

		String path = request.getRequestURI().substring(request.getContextPath().length());

		// --- FIX IS HERE: Add a comprehensive null-check ---
        // Check if the session exists AND if the user object is in the session.
		if (session == null || session.getAttribute("user") == null) {
			logger.warn("Admin access DENIED to path '{}' because there is no active session. Redirecting to login.", path);
			response.sendRedirect(request.getContextPath() + "/login");
			return; // Stop processing this request.
		}

		User user = (User) session.getAttribute("user");
		logger.debug("AdminFilter processing request for path: '{}' for user: '{}'", path, user.getUsername());

		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			logger.info("ADMIN access GRANTED for user '{}' to path '{}'. Passing to next filter/servlet.",
					user.getUsername(), path);
			chain.doFilter(request, response);
		} else {
			logger.warn("ADMIN access DENIED for user '{}' (Role: '{}') to path '{}'. Redirecting to user home.",
					user.getUsername(), user.getRole(), path);
			request.getSession().setAttribute("accessErrorMessage", "Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");
			response.sendRedirect(request.getContextPath() + "/home");
		}
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}