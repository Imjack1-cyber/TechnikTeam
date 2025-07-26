package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class UpdateWikiAction implements Action {
	private static final Logger logger = LogManager.getLogger(UpdateWikiAction.class);
	private final WikiDAO wikiDAO;

	@Inject
	public UpdateWikiAction(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response) {
		try {
			int wikiId = Integer.parseInt(request.getParameter("wikiId"));
			String content = request.getParameter("content");

			if (content == null) {
				logger.warn("Update attempt for wikiId {} with null content.", wikiId);
				return new ApiResponse(false, "Content cannot be null.", null);
			}

			boolean success = wikiDAO.updateWikiContent(wikiId, content);

			if (success) {
				logger.info("Successfully updated wiki entry with ID: {}", wikiId);
				return new ApiResponse(true, "Page updated successfully.", null);
			} else {
				logger.error("Failed to update wiki entry with ID: {} in database.", wikiId);
				return new ApiResponse(false, "Failed to update page in database.", null);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in UpdateWikiAction: {}", request.getParameter("wikiId"), e);
			return new ApiResponse(false, "Invalid ID format.", null);
		}
	}
}