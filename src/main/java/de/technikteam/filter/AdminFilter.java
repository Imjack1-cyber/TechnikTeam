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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebFilter(urlPatterns = { "/admin/*", "/api/admin/*" }, asyncSupported = true)
public class AdminFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AdminFilter.class);
	private final Map<String, String> permissionMap = new HashMap<>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		permissionMap.put("/admin/mitglieder", "USER_READ");
		permissionMap.put("/admin/veranstaltungen", "EVENT_READ");
		permissionMap.put("/admin/lehrgaenge", "COURSE_READ");
		permissionMap.put("/admin/meetings", "COURSE_READ");
		permissionMap.put("/admin/lager", "STORAGE_READ");
		permissionMap.put("/admin/defekte", "STORAGE_READ");
		permissionMap.put("/admin/kits", "KIT_READ");
		permissionMap.put("/admin/matrix", "QUALIFICATION_READ");
		permissionMap.put("/admin/teilnahme", "QUALIFICATION_UPDATE");
		permissionMap.put("/admin/dateien", "FILE_READ");
		permissionMap.put("/admin/log", "LOG_READ");
		permissionMap.put("/admin/berichte", "REPORT_READ");
		permissionMap.put("/admin/system", "SYSTEM_READ");
		permissionMap.put("/api/admin/system-stats", "SYSTEM_READ");

		logger.info("AdminFilter initialized and protecting /admin/* and /api/admin/* paths with dynamic permissions.");
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
		Set<String> permissions = user.getPermissions();

		if (permissions == null) {
			logger.error("Permissions set is NULL on user object in session for user '{}'. Denying access.",
					user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Berechtigungen nicht geladen.");
			return;
		}

		if (permissions.contains("ACCESS_ADMIN_PANEL")) {
			logger.debug("ADMIN access GRANTED for user '{}' to path '{}' via master permission.", user.getUsername(),
					path);
			chain.doFilter(request, response);
			return;
		}

		if (path.equals("/admin/dashboard") && permissions.stream().anyMatch(p -> p.contains("_READ")
				|| p.contains("_MANAGE") || p.contains("_UPDATE") || p.contains("_CREATE") || p.contains("_DELETE"))) {
			logger.debug("Admin dashboard access GRANTED for user '{}' due to having at least one sub-permission.",
					user.getUsername());
			chain.doFilter(request, response);
			return;
		}

		for (Map.Entry<String, String> entry : permissionMap.entrySet()) {
			if (path.startsWith(entry.getKey()) && permissions.contains(entry.getValue())) {
				logger.debug("Specific admin access GRANTED for user '{}' to path '{}' via permission '{}'.",
						user.getUsername(), path, entry.getValue());
				chain.doFilter(request, response);
				return;
			}
		}

		logger.warn(
				"ADMIN access DENIED for user '{}' (Role: '{}') to path '{}'. Insufficient permissions. Redirecting to user home.",
				user.getUsername(), user.getRoleName(), path);
		request.getSession().setAttribute("accessErrorMessage",
				"Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");
		response.sendError(HttpServletResponse.SC_FORBIDDEN,
				"Sie haben keine Berechtigung, auf diese Seite zuzugreifen.");
	}

	@Override
	public void destroy() {
		logger.info("AdminFilter destroyed.");
	}
}