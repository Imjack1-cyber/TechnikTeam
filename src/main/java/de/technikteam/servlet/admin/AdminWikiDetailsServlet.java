package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
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
	private final WikiDAO wikiDAO;

	@Inject
	public AdminWikiDetailsServlet(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
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

		String idParam = request.getParameter("id");
		if (idParam == null || idParam.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID parameter is missing.");
			return;
		}

		try {
			int id = Integer.parseInt(idParam);
			WikiEntry entry = wikiDAO.getWikiEntryById(id);

			if (entry == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Wiki entry not found.");
				return;
			}

			request.setAttribute("wikiEntry", entry);
			request.getRequestDispatcher("/views/admin/admin_wiki_details.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format.");
		}
	}
}