package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.WikiEntry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;

@Singleton
public class GetWikiContentAction implements Action {
	private static final Logger logger = LogManager.getLogger(GetWikiContentAction.class);
	private final WikiDAO wikiDAO;

	@Inject
	public GetWikiContentAction(WikiDAO wikiDAO) {
		this.wikiDAO = wikiDAO;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response) {
		try {
			int id = Integer.parseInt(request.getParameter("id"));
			Optional<WikiEntry> entryOptional = wikiDAO.getWikiEntryById(id);

			if (entryOptional.isPresent()) {
				return new ApiResponse(true, "Content loaded.", entryOptional.get());
			} else {
				logger.warn("Wiki content requested for non-existent ID: {}", id);
				return new ApiResponse(false, "Entry not found.", null);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in GetWikiContentAction: {}", request.getParameter("id"), e);
			return new ApiResponse(false, "Invalid ID format.", null);
		}
	}
}