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
import java.util.Set;

@WebFilter(urlPatterns = "/admin/*", asyncSupported = true)
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);

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
			response.sendRedirect(request.getContextPath() + "/WEB-INF/views/auth/login.jsp");
			return;
		}

		User user = (User) session.getAttribute("user");
		Set<String> permissions = user.getPermissions();

		if (permissions == null) {
			logger.error("Permissions set is NULL on user object in session for user '{}'. Denying access.",
					user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Berechtigungen nicht geladen.");
			return;
		}

		// CORRECTION: This is the main logical fix.
		// If the user has the master permission, let them access ANY /admin/* page.
		if (permissions.contains("ACCESS_ADMIN_PANEL")) {
			logger.debug("ADMIN access GRANTED for user '{}' to path '{}' via master permission.", user.getUsername(),
					path);
			chain.doFilter(request, response);
			return; // <<<<< IMPORTANT: Return here to stop further checks.
		}

		// This block is now ONLY for users who are NOT full admins, like LAGERWART.
		boolean hasSpecificPermission = false;
		if (path.startsWith("/admin/storage") && permissions.contains("STORAGE_MANAGE")) {
			hasSpecificPermission = true;
		} else if (path.startsWith("/WEB-INF/views/admin/admin_defect_list.jsp") && permissions.contains("DEFECTS_MANAGE")) {
			hasSpecificPermission = true;
		}

		if (hasSpecificPermission) {
			logger.debug("Specific admin access GRANTED for user '{}' (Role: '{}') to path '{}'.", user.getUsername(),
					user.getRoleName(), path);
			chain.doFilter(request, response);
			return;
		}

		// If the user is not a full admin and does not have specific permission for the
		// page, deny access.
		logger.warn("ADMIN access DENIED for user '{}' (Role: '{}') to path '{}'. Redirecting to user home.",
				user.getUsername(), user.getRoleName(), path);
		request.getSession().setAttribute("accessErrorMessage",
				"Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");
		response.sendRedirect(request.getContextPath() + "/home");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("AdminFilter initialized and protecting /admin/* paths with dynamic permissions.");
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}