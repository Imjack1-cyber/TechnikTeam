package de.technikteam.filter;

import de.technikteam.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebFilter(urlPatterns = { "/admin/*", "/api/admin/*" }, asyncSupported = true)
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AdminFilter initialized and protecting /admin/* and /api/admin/* paths.");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);
		String path = request.getRequestURI().substring(request.getContextPath().length());
		logger.trace("AdminFilter is processing request for path: '{}'", path);

		if (session == null || session.getAttribute("user") == null) {
			logger.warn("Admin access DENIED to path '{}'. No active session found. Redirecting to login.", path);
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = (User) session.getAttribute("user");

		if (user.hasAdminAccess()) {
			logger.debug(
					"ADMIN area access GRANTED for user '{}' to path '{}'. User has at least one admin permission.",
					user.getUsername(), path);
			chain.doFilter(request, response);
		} else {
			logger.warn(
					"ADMIN access DENIED for user '{}' (Role: '{}') to path '{}'. No relevant admin permissions found.",
					user.getUsername(), user.getRoleName(), path);
			request.getSession().setAttribute("accessErrorMessage",
					"Sie haben keine Berechtigung, auf den Admin-Bereich zuzugreifen.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Sie haben keine Berechtigung, auf diesen Bereich zuzugreifen.");
		}
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}