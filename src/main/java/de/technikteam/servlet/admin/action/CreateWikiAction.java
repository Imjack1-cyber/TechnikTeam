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

@Singleton
public class CreateWikiAction implements Action {

	private final WikiDAO wikiDAO;
	private final AdminLogService adminLogService;

	@Inject
	public CreateWikiAction(WikiDAO wikiDAO, AdminLogService adminLogService) {
		this.wikiDAO = wikiDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response) {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			return ApiResponse.error("Access Denied.");
		}

		String filePath = request.getParameter("filePath");
		String content = request.getParameter("content");

		if (filePath == null || filePath.trim().isEmpty()) {
			return ApiResponse.error("File path cannot be empty.");
		}

		WikiEntry newEntry = new WikiEntry();
		newEntry.setFilePath(filePath.trim());
		newEntry.setContent(content);

		WikiEntry createdEntry = wikiDAO.createWikiEntry(newEntry);

		if (createdEntry != null) {
			adminLogService.log(adminUser.getUsername(), "WIKI_CREATE",
					"Created documentation for file: " + createdEntry.getFilePath());
			return ApiResponse.success("Wiki page created successfully.", createdEntry);
		} else {
			return ApiResponse.error("Failed to create wiki page. The file path may already exist.");
		}
	}
}