// src/main/java/de/technikteam/api/v1/KitResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.InventoryKitItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A stateless, resource-oriented REST API endpoint for managing inventory kits.
 * Mapped to /api/v1/kits/*
 */
@Singleton
public class KitResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(KitResource.class);

	private final InventoryKitDAO kitDAO;
	private final AdminLogService adminLogService;
	private final Gson gson;

	@Inject
	public KitResource(InventoryKitDAO kitDAO, AdminLogService adminLogService, Gson gson) {
		this.kitDAO = kitDAO;
		this.adminLogService = adminLogService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.KIT_READ))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		try {
			if (pathInfo == null || pathInfo.equals("/")) {
				List<InventoryKit> kits = kitDAO.getAllKitsWithItems();
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Kits retrieved successfully", kits));
			} else {
				Integer kitId = parseIdFromPath(pathInfo);
				if (kitId != null) {
					InventoryKit kit = kitDAO.getKitById(kitId);
					if (kit != null) {
						kit.setItems(kitDAO.getItemsForKit(kitId));
						sendJsonResponse(resp, HttpServletResponse.SC_OK,
								new ApiResponse(true, "Kit retrieved successfully", kit));
					} else {
						sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Kit not found");
					}
				} else {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid kit ID format");
				}
			}
		} catch (Exception e) {
			logger.error("Error processing GET request for kits", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.KIT_CREATE))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			InventoryKit newKit = gson.fromJson(jsonPayload, InventoryKit.class);

			int newId = kitDAO.createKit(newKit);
			if (newId > 0) {
				newKit.setId(newId);
				adminLogService.log(adminUser.getUsername(), "CREATE_KIT_API",
						"Kit '" + newKit.getName() + "' created via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "Kit created successfully", newKit));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"Kit could not be created (name may already exist).");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing POST request to create kit", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.KIT_UPDATE))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing kit ID.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		Integer kitId = parseId(pathParts[0]);
		if (kitId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid kit ID in URL.");
			return;
		}

		// Check for sub-resource path: /api/v1/kits/{id}/items
		if (pathParts.length == 2 && "items".equals(pathParts[1])) {
			handleUpdateKitItems(req, resp, adminUser, kitId);
		} else if (pathParts.length == 1) {
			handleUpdateKitMetadata(req, resp, adminUser, kitId);
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	private void handleUpdateKitMetadata(HttpServletRequest req, HttpServletResponse resp, User adminUser, int kitId)
			throws IOException {
		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			InventoryKit updatedKit = gson.fromJson(jsonPayload, InventoryKit.class);
			updatedKit.setId(kitId);

			if (kitDAO.updateKit(updatedKit)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_KIT_API",
						"Kit metadata for '" + updatedKit.getName() + "' (ID: " + kitId + ") updated via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Kit updated successfully", updatedKit));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Kit not found or update failed.");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update kit metadata", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	private void handleUpdateKitItems(HttpServletRequest req, HttpServletResponse resp, User adminUser, int kitId)
			throws IOException {
		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Type listType = new TypeToken<List<InventoryKitItem>>() {
			}.getType();
			List<InventoryKitItem> items = gson.fromJson(jsonPayload, listType);

			String[] itemIds = items.stream().map(i -> String.valueOf(i.getItemId())).toArray(String[]::new);
			String[] quantities = items.stream().map(i -> String.valueOf(i.getQuantity())).toArray(String[]::new);

			if (kitDAO.updateKitItems(kitId, itemIds, quantities)) {
				adminLogService.log(adminUser.getUsername(), "UPDATE_KIT_ITEMS_API",
						"Contents for kit ID " + kitId + " updated via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Kit items updated successfully", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update kit items.");
			}
		} catch (JsonSyntaxException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
					"Invalid JSON format. Expected an array of objects with 'itemId' and 'quantity'.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update kit items", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || (!adminUser.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)
				&& !adminUser.getPermissions().contains(Permissions.KIT_DELETE))) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer kitId = parseIdFromPath(req.getPathInfo());
		if (kitId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid kit ID in URL.");
			return;
		}

		InventoryKit kitToDelete = kitDAO.getKitById(kitId);
		if (kitToDelete == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Kit to delete not found.");
			return;
		}

		if (kitDAO.deleteKit(kitId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_KIT_API",
					"Kit '" + kitToDelete.getName() + "' (ID: " + kitId + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Kit deleted successfully", Map.of("deletedId", kitId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete kit.");
		}
	}

	private Integer parseId(String pathSegment) {
		try {
			return Integer.parseInt(pathSegment);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1)
			return null;
		return parseId(pathInfo.substring(1));
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