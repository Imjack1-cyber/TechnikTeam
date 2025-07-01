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

		// 1. Check if user is logged in at all.
		if (session == null || session.getAttribute("user") == null) {
			logger.warn("Admin access DENIED to path '{}'. No active session found. Redirecting to login.", path);
			// Redirect to the /login servlet, not a direct JSP.
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = (User) session.getAttribute("user");
		Set<String> permissions = user.getPermissions();

		// 2. Check if the user's permissions were loaded correctly.
		if (permissions == null) {
			logger.error("Permissions set is NULL on user object in session for user '{}'. Denying access.",
					user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Berechtigungen nicht geladen.");
			return;
		}

		// 3. Check for the master "ACCESS_ADMIN_PANEL" permission. This grants access
		// to everything under /admin/*.
		if (permissions.contains("ACCESS_ADMIN_PANEL")) {
			logger.debug("ADMIN access GRANTED for user '{}' to path '{}' via master permission.", user.getUsername(),
					path);
			chain.doFilter(request, response);
			return;
		}

		// 4. If the user is NOT a full admin, check for specific, limited permissions.
		boolean hasSpecificPermission = false;
		// CORRECTED: The path for the storage servlet is /admin/lager, not
		// /admin/storage.
		if (path.startsWith("/admin/lager") && permissions.contains("STORAGE_MANAGE")) {
			hasSpecificPermission = true;
		} else if (path.startsWith("/admin/defekte") && permissions.contains("DEFECTS_MANAGE")) {
			hasSpecificPermission = true;
		}
		// Add other 'else if' blocks here for any other specific, limited admin roles.

		if (hasSpecificPermission) {
			logger.debug("Specific admin access GRANTED for user '{}' (Role: '{}') to path '{}'.", user.getUsername(),
					user.getRoleName(), path);
			chain.doFilter(request, response);
			return;
		}

		// 5. If the user is not a full admin and has no specific permissions for the
		// requested page, deny access.
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