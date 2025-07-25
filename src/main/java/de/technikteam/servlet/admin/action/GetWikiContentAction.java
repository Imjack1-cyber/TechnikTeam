package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Singleton
public class GetWikiContentAction implements Action {

	private final WikiDAO wikiDAO;

	@Inject
	public GetWikiContentAction(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null || !user.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			return ApiResponse.error("Access Denied.");
		}

		try {
			int id = Integer.parseInt(request.getParameter("id"));
			WikiEntry entry = wikiDAO.getWikiEntryById(id);
			if (entry == null) {
				return ApiResponse.error("Wiki entry not found.");
			}
			return ApiResponse.success("Content loaded.", entry);
		} catch (NumberFormatException e) {
			return ApiResponse.error("Invalid ID format.");
		}
	}
}