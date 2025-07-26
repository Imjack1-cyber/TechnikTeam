package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.WikiEntry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class CreateWikiAction implements Action {
	private static final Logger logger = LogManager.getLogger(CreateWikiAction.class);
	private final WikiDAO wikiDAO;

	@Inject
	public CreateWikiAction(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String filePath = request.getParameter("filePath");
		String content = request.getParameter("content");

		if (filePath == null || filePath.isBlank()) {
			return new ApiResponse(false, "File path cannot be empty.", null);
		}

		if (wikiDAO.findByFilePath(filePath).isPresent()) {
			logger.warn("Attempted to create a wiki entry with a duplicate file path: {}", filePath);
			return new ApiResponse(false, "An entry with this file path already exists.", null);
		}

		WikiEntry newEntry = new WikiEntry();
		newEntry.setFilePath(filePath);
		newEntry.setContent(content);

		Optional<WikiEntry> createdEntryOptional = wikiDAO.createWikiEntry(newEntry);

		if (createdEntryOptional.isPresent()) {
			WikiEntry createdEntry = createdEntryOptional.get();
			logger.info("Successfully created new wiki entry with ID: {}", createdEntry.getId());
			return new ApiResponse(true, "Page created successfully.", createdEntry);
		} else {
			logger.error("Failed to create wiki entry for path: {}", filePath);
			return new ApiResponse(false, "Failed to create page in database.", null);
		}
	}
}