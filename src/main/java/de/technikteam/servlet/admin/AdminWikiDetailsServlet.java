package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.model.User;
import de.technikteam.service.WikiService;
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

	private final WikiService wikiService;

	@Inject
	public AdminWikiDetailsServlet(WikiService wikiService) {
		this.wikiService = wikiService;
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

		String wikiContent = wikiService.getFileDocumentation(filePath);

		request.setAttribute("filePath", filePath);
		request.setAttribute("wikiContent", wikiContent);
		request.getRequestDispatcher("/views/admin/admin_wiki_details.jsp").forward(request, response);
	}
}