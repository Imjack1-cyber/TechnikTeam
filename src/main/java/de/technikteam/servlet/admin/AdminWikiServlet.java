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
public class AdminWikiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminWikiServlet.class);

	private final WikiService wikiService;

	@Inject
	public AdminWikiServlet(WikiService wikiService) {
		this.wikiService = wikiService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		if (user == null || !user.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			logger.warn("Unauthorized access attempt to admin wiki by user '{}'",
					user != null ? user.getUsername() : "GUEST");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this page.");
			return;
		}

		request.setAttribute("projectTreeHtml", wikiService.getProjectTreeHtml());
		request.getRequestDispatcher("/views/admin/admin_wiki_index.jsp").forward(request, response);
	}
}