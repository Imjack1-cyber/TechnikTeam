package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.MarkdownUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class UpdateWikiAction implements Action {
	private final WikiDAO wikiDAO;
	private final AdminLogService adminLogService;

	@Inject
	public UpdateWikiAction(WikiDAO wikiDAO, AdminLogService adminLogService) {
		this.wikiDAO = wikiDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			return ApiResponse.error("Access Denied.");
		}

		try {
			int wikiId = Integer.parseInt(request.getParameter("wikiId"));
			String content = request.getParameter("content");
			String sanitizedContent = MarkdownUtil.sanitize(content);

			WikiEntry originalEntry = wikiDAO.getWikiEntryById(wikiId);
			if (originalEntry == null) {
				return ApiResponse.error("Wiki entry not found.");
			}

			if (wikiDAO.updateWikiContent(wikiId, sanitizedContent)) {
				adminLogService.log(adminUser.getUsername(), "WIKI_UPDATE",
						"Updated documentation for file: " + originalEntry.getFilePath());
				return ApiResponse.success("Documentation updated successfully.");
			} else {
				return ApiResponse.error("Failed to update documentation in the database.");
			}
		} catch (NumberFormatException e) {
			return ApiResponse.error("Invalid wiki ID format.");
		} catch (Exception e) {
			return ApiResponse.error("An internal error occurred: " + e.getMessage());
		}
	}
}