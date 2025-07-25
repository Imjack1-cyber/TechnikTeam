package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Singleton
public class AdminWikiDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminWikiDetailsServlet.class);

	@Inject
	public AdminWikiDetailsServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		if (user == null || !user.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			logger.warn("Unauthorized access attempt to admin wiki details by user '{}'",
					user != null ? user.getUsername() : "GUEST");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this page.");
			return;
		}

		String filePath = request.getParameter("file");
		if (filePath == null || filePath.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File parameter is missing.");
			return;
		}

		if (filePath.contains("..")) {
			logger.error("Path traversal attempt detected in AdminWikiDetailsServlet for file: {}", filePath);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file path.");
			return;
		}

		String jspPath = "/views/admin/wiki/" + filePath + ".jsp";

		if (getServletContext().getResource(jspPath) == null) {
			logger.warn("Requested wiki JSP page does not exist: {}. Forwarding to template.", jspPath);
			request.setAttribute("filePath", filePath);
			request.getRequestDispatcher("/views/admin/wiki/_template.jsp").forward(request, response);
		} else {
			logger.debug("Forwarding to wiki page: {}", jspPath);
			request.setAttribute("filePath", filePath);
			request.getRequestDispatcher(jspPath).forward(request, response);
		}
	}
}