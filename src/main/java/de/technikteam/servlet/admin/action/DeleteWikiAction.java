package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@Singleton
public class DeleteWikiAction implements Action {

	private final WikiDAO wikiDAO;
	private final AdminLogService adminLogService;

	@Inject
	public DeleteWikiAction(WikiDAO wikiDAO, AdminLogService adminLogService) {
		this.wikiDAO = wikiDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response) {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			return ApiResponse.error("Access Denied.");
		}

		try {
			int wikiId = Integer.parseInt(request.getParameter("wikiId"));
			WikiEntry entryToDelete = wikiDAO.getWikiEntryById(wikiId);
			if (entryToDelete == null) {
				return ApiResponse.error("Wiki entry not found.");
			}

			if (wikiDAO.deleteWikiEntry(wikiId)) {
				adminLogService.log(adminUser.getUsername(), "WIKI_DELETE",
						"Deleted documentation for file: " + entryToDelete.getFilePath());
				return ApiResponse.success("Wiki page deleted successfully.", Map.of("deletedId", wikiId));
			} else {
				return ApiResponse.error("Failed to delete wiki page.");
			}
		} catch (NumberFormatException e) {
			return ApiResponse.error("Invalid wiki ID format.");
		}
	}
}