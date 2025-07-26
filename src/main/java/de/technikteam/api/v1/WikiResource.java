// src/main/java/de/technikteam/api/v1/WikiResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.WikiDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.WikiEntry;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.WikiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A stateless, resource-oriented REST API endpoint for managing the technical
 * documentation wiki. This servlet handles all CRUD operations for the Wiki
 * resource. Mapped to /api/v1/wiki/*
 */
@Singleton
public class WikiResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(WikiResource.class);

	private final WikiService wikiService;
	private final WikiDAO wikiDAO;
	private final AdminLogService adminLogService;
	private final Gson gson;

	@Inject
	public WikiResource(WikiService wikiService, WikiDAO wikiDAO, AdminLogService adminLogService, Gson gson) {
		this.wikiService = wikiService;
		this.wikiDAO = wikiDAO;
		this.adminLogService = adminLogService;
		this.gson = gson;
	}

	/**
	 * Handles GET requests. GET /api/v1/wiki -> Returns the entire wiki navigation
	 * tree. GET /api/v1/wiki/{id} -> Returns the content of a single wiki page.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();

		try {
			if (pathInfo == null || pathInfo.equals("/")) {
				// Get the entire wiki tree structure
				Map<String, Object> treeData = wikiService.getWikiTreeAsData();
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Wiki tree retrieved successfully", treeData));
			} else {
				// Get content of a single wiki page by ID
				Integer wikiId = parseIdFromPath(pathInfo);
				if (wikiId != null) {
					Optional<WikiEntry> entryOptional = wikiDAO.getWikiEntryById(wikiId);
					if (entryOptional.isPresent()) {
						sendJsonResponse(resp, HttpServletResponse.SC_OK,
								new ApiResponse(true, "Content loaded", entryOptional.get()));
					} else {
						sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Wiki entry not found");
					}
				} else {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid wiki ID format");
				}
			}
		} catch (Exception e) {
			logger.error("Error processing GET request for wiki", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles POST requests. POST /api/v1/wiki -> Creates a new wiki page from a
	 * JSON body.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			WikiEntry newEntry = gson.fromJson(jsonPayload, WikiEntry.class);

			if (newEntry.getFilePath() == null || newEntry.getFilePath().isBlank()) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "File path cannot be empty.");
				return;
			}
			if (wikiDAO.findByFilePath(newEntry.getFilePath()).isPresent()) {
				sendJsonError(resp, HttpServletResponse.SC_CONFLICT, "An entry with this file path already exists.");
				return;
			}

			Optional<WikiEntry> createdEntryOptional = wikiDAO.createWikiEntry(newEntry);

			if (createdEntryOptional.isPresent()) {
				WikiEntry createdEntry = createdEntryOptional.get();
				adminLogService.log(adminUser.getUsername(), "CREATE_WIKI_PAGE",
						"Created wiki page: " + createdEntry.getFilePath());
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "Page created successfully", createdEntry));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create page in database.");
			}
		} catch (JsonSyntaxException e) {
			logger.warn("Invalid JSON format for create wiki request", e);
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing POST request to create wiki page", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles PUT requests. PUT /api/v1/wiki/{id} -> Updates an existing wiki page
	 * from a JSON body.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer wikiId = parseIdFromPath(req.getPathInfo());
		if (wikiId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid wiki ID in URL.");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Map<String, String> payload = gson.fromJson(jsonPayload, Map.class);
			String content = payload.get("content");

			if (content == null) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Content cannot be null.");
				return;
			}

			if (wikiDAO.updateWikiContent(wikiId, content)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_WIKI_PAGE", "Updated wiki page ID: " + wikiId);
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Page updated successfully", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to update page. It may not exist.");
			}
		} catch (JsonSyntaxException e) {
			logger.warn("Invalid JSON format for update wiki request", e);
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update wiki page", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles DELETE requests. DELETE /api/v1/wiki/{id} -> Deletes a wiki page.
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer wikiId = parseIdFromPath(req.getPathInfo());
		if (wikiId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid wiki ID in URL.");
			return;
		}

		Optional<WikiEntry> entryToDelete = wikiDAO.getWikiEntryById(wikiId);
		if (entryToDelete.isEmpty()) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Wiki entry not found.");
			return;
		}

		if (wikiDAO.deleteWikiEntry(wikiId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_WIKI_PAGE",
					"Deleted wiki page: " + entryToDelete.get().getFilePath());
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Page deleted successfully", Map.of("deletedId", wikiId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete page.");
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1) {
			return null;
		}
		try {
			return Integer.parseInt(pathInfo.substring(1));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}