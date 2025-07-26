// src/main/java/de/technikteam/api/v1/StorageResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken; // <-- MISSING IMPORT ADDED
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.MaintenanceLogDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.MaintenanceLogEntry;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.ConfigurationService;
import de.technikteam.service.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type; // <-- MISSING IMPORT ADDED
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@MultipartConfig(maxFileSize = 5242880, // 5MB
		maxRequestSize = 10485760, // 10MB
		fileSizeThreshold = 1048576 // 1MB
)
public class StorageResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageResource.class);

	private final StorageDAO storageDAO;
	private final MaintenanceLogDAO maintenanceLogDAO;
	private final StorageService storageService;
	private final AdminLogService adminLogService;
	private final ConfigurationService configService;
	private final Gson gson;

	@Inject
	public StorageResource(StorageDAO storageDAO, MaintenanceLogDAO maintenanceLogDAO, StorageService storageService,
			AdminLogService adminLogService, ConfigurationService configService, Gson gson) {
		this.storageDAO = storageDAO;
		this.maintenanceLogDAO = maintenanceLogDAO;
		this.storageService = storageService;
		this.adminLogService = adminLogService;
		this.configService = configService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("STORAGE_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		String statusFilter = req.getParameter("status");

		if (pathInfo == null || pathInfo.equals("/")) {
			List<StorageItem> items;
			if ("defective".equalsIgnoreCase(statusFilter)) {
				items = storageDAO.getDefectiveItems();
			} else {
				items = storageDAO.getAllItems();
			}
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Items retrieved successfully", items));
		} else {
			Integer itemId = parseIdFromPath(pathInfo);
			if (itemId != null) {
				StorageItem item = storageDAO.getItemById(itemId);
				if (item != null) {
					sendJsonResponse(resp, HttpServletResponse.SC_OK,
							new ApiResponse(true, "Item retrieved successfully", item));
				} else {
					sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Item not found");
				}
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID format");
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		boolean isUpdate = (pathInfo != null && !pathInfo.equals("/"));
		User adminUser = (User) req.getAttribute("user");
		String requiredPermission = isUpdate ? "STORAGE_UPDATE" : "STORAGE_CREATE";
		if (adminUser == null || !adminUser.getPermissions().contains(requiredPermission)) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			StorageItem item = new StorageItem();
			item.setName(req.getParameter("name"));
			item.setLocation(req.getParameter("location"));
			item.setCabinet(req.getParameter("cabinet"));
			item.setCompartment(req.getParameter("compartment"));
			item.setQuantity(Integer.parseInt(req.getParameter("quantity")));
			item.setMaxQuantity(Integer.parseInt(req.getParameter("maxQuantity")));
			String weightStr = req.getParameter("weight_kg");
			item.setWeightKg(
					weightStr == null || weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr.replace(',', '.')));
			String priceStr = req.getParameter("price_eur");
			item.setPriceEur(
					priceStr == null || priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr.replace(',', '.')));

			String imagePath = null;
			if (isUpdate) {
				Integer itemId = parseIdFromPath(pathInfo);
				item.setId(itemId);
				StorageItem originalItem = storageDAO.getItemById(itemId);
				if (originalItem != null) {
					imagePath = originalItem.getImagePath();
					// Preserve existing status fields not editable in this form
					item.setDefectiveQuantity(originalItem.getDefectiveQuantity());
					item.setDefectReason(originalItem.getDefectReason());
					item.setStatus(originalItem.getStatus());
				}
			}

			Part filePart = req.getPart("imageFile");
			if (filePart != null && filePart.getSize() > 0) {
				String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
				if (!fileName.isEmpty()) {
					File imageUploadDir = new File(configService.getProperty("upload.directory"), "images");
					if (!imageUploadDir.exists())
						imageUploadDir.mkdirs();
					File targetFile = new File(imageUploadDir, fileName);
					filePart.write(targetFile.getAbsolutePath());
					imagePath = fileName;
				}
			}
			item.setImagePath(imagePath);

			boolean success = isUpdate ? storageDAO.updateItem(item) : storageDAO.createItem(item);
			if (success) {
				String logAction = isUpdate ? "UPDATE_STORAGE_ITEM_API" : "CREATE_STORAGE_ITEM_API";
				adminLogService.log(adminUser.getUsername(), logAction,
						"Storage item '" + item.getName() + "' was saved via API.");
				sendJsonResponse(resp, isUpdate ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "Item saved successfully", item));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save item to database.");
			}
		} catch (Exception e) {
			logger.error("Error processing POST request for storage item", e);
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid data provided: " + e.getMessage());
		}
	}

	/**
	 * Handles PUT requests for partial updates (e.g., status changes). Replaces the
	 * need for a PATCH method, which is not standard in HttpServlet.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("STORAGE_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer itemId = parseIdFromPath(req.getPathInfo());
		if (itemId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID in URL.");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			// CORRECTED: Added the necessary Type object for Gson deserialization.
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> payload = gson.fromJson(jsonPayload, type);
			String action = (String) payload.get("action");

			boolean success = false;
			switch (action) {
			case "report_defect":
				int defectQty = ((Double) payload.get("quantity")).intValue();
				String reason = (String) payload.get("reason");
				success = storageService.updateDefectiveItemStatus(itemId, "DEFECT", defectQty, reason, adminUser);
				break;
			case "report_unrepairable":
				int unrepairableQty = ((Double) payload.get("quantity")).intValue();
				String unrepairableReason = (String) payload.get("reason");
				success = storageService.updateDefectiveItemStatus(itemId, "UNREPAIRABLE", unrepairableQty,
						unrepairableReason, adminUser);
				break;
			case "repair":
				int repairedQty = ((Double) payload.get("quantity")).intValue();
				String repairNotes = (String) payload.get("notes");
				success = storageDAO.repairItems(itemId, repairedQty);
				if (success) {
					MaintenanceLogEntry log = new MaintenanceLogEntry();
					log.setItemId(itemId);
					log.setUserId(adminUser.getId());
					log.setAction(repairedQty + " units repaired");
					log.setNotes(repairNotes);
					maintenanceLogDAO.createLog(log);
					adminLogService.log(adminUser.getUsername(), "REPAIR_ITEM_API",
							String.format("%d units of item ID %d marked as repaired via API. Notes: %s", repairedQty,
									itemId, repairNotes));
				}
				break;
			case "set_maintenance_status":
				String newStatus = (String) payload.get("status");
				String maintenanceNotes = (String) payload.get("notes");
				success = storageDAO.updateItemStatus(itemId, newStatus);
				if (success) {
					MaintenanceLogEntry log = new MaintenanceLogEntry();
					log.setItemId(itemId);
					log.setUserId(adminUser.getId());
					log.setAction("Status changed to " + newStatus);
					log.setNotes(maintenanceNotes);
					maintenanceLogDAO.createLog(log);
					adminLogService.log(adminUser.getUsername(), "UPDATE_ITEM_STATUS_API",
							"Status for item ID " + itemId + " set to '" + newStatus + "' via API.");
				}
				break;
			default:
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action in request body.");
				return;
			}

			if (success) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Item status updated successfully.", storageDAO.getItemById(itemId)));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"Failed to update item status. Check quantities and logs.");
			}
		} catch (Exception e) {
			logger.error("Error processing PUT for storage item {}", itemId, e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An internal error occurred: " + e.getMessage());
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("STORAGE_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer itemId = parseIdFromPath(req.getPathInfo());
		if (itemId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID in URL.");
			return;
		}

		StorageItem item = storageDAO.getItemById(itemId);
		if (item == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Item to delete not found.");
			return;
		}

		if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
			File imageDir = new File(configService.getProperty("upload.directory"), "images");
			File imageFile = new File(imageDir, item.getImagePath());
			if (imageFile.exists() && !imageFile.delete()) {
				logger.warn("Could not delete physical image file: {}", imageFile.getAbsolutePath());
			}
		}

		if (storageDAO.deleteItem(itemId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_STORAGE_ITEM_API",
					"Storage item '" + item.getName() + "' (ID: " + itemId + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Item deleted successfully", Map.of("deletedId", itemId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete item from database.");
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1)
			return null;
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