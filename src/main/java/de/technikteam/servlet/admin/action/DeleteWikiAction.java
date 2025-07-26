// FILE: /src/main/java/de/technikteam/servlet/admin/action/DeleteWikiAction.java
package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

@Singleton
public class DeleteWikiAction implements Action {
	private static final Logger logger = LogManager.getLogger(DeleteWikiAction.class);
	private final WikiDAO wikiDAO;

	@Inject
	public DeleteWikiAction(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int wikiId = Integer.parseInt(request.getParameter("wikiId"));
			boolean success = wikiDAO.deleteWikiEntry(wikiId);

			if (success) {
				logger.info("Successfully deleted wiki entry with ID: {}", wikiId);
				return new ApiResponse(true, "Page deleted successfully.", null);
			} else {
				logger.warn("Attempted to delete a non-existent wiki entry with ID: {}", wikiId);
				return new ApiResponse(false, "Failed to delete page. It may have already been deleted.", null);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in DeleteWikiAction: {}", request.getParameter("wikiId"), e);
			return new ApiResponse(false, "Invalid ID format.", null);
		}
	}
}