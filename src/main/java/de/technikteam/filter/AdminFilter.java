package de.technikteam.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.User;

// This filter protects all URLs under the /admin/ path.
// It ensures that only users with the 'ADMIN' role can access these pages.
@WebFilter(value = "/admin/*", asyncSupported = true)
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);

		// The AuthenticationFilter should have already ensured the user is logged in.
		// This filter performs the next level of authorization check.
		User user = (session != null) ? (User) session.getAttribute("user") : null;

		if (user != null && "ADMIN".equals(user.getRole())) {
			// User is an ADMIN, allow access to the requested admin resource.
			logger.debug("Admin access granted for user '{}' to URI: {}", user.getUsername(), request.getRequestURI());
			chain.doFilter(request, response);
		} else {
			// User is not an admin or not logged in properly.
			String username = (user != null) ? user.getUsername() : "Guest";
			logger.warn("Admin access denied for user '{}' to URI: {}. Redirecting to home.", username,
					request.getRequestURI());

			// Set an error message to display on the home page.
			request.getSession().setAttribute("accessErrorMessage",
					"Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");

			// Redirect to the user home page.
			response.sendRedirect(request.getContextPath() + "/home");
		}
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		logger.info("AdminFilter initialized.");
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}