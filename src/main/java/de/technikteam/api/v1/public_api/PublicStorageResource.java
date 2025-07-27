// src/main/java/de/technikteam/api/v1/public_api/PublicStorageResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MaintenanceLogDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.*;
import de.technikteam.service.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class PublicStorageResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PublicStorageResource.class);

	private final StorageDAO storageDAO;
	private final EventDAO eventDAO;
	private final StorageService storageService;
	private final StorageLogDAO storageLogDAO;
	private final MaintenanceLogDAO maintenanceLogDAO;
	private final Gson gson;

	@Inject
	public PublicStorageResource(StorageDAO storageDAO, EventDAO eventDAO, StorageService storageService,
			StorageLogDAO storageLogDAO, MaintenanceLogDAO maintenanceLogDAO, Gson gson) {
		this.storageDAO = storageDAO;
		this.eventDAO = eventDAO;
		this.storageService = storageService;
		this.storageLogDAO = storageLogDAO;
		this.maintenanceLogDAO = maintenanceLogDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			handleGetStorageList(req, resp);
		} else {
			String[] pathParts = pathInfo.substring(1).split("/");
			Integer itemId = parseId(pathParts[0]);
			if (itemId != null) {
				if (pathParts.length == 2 && "history".equals(pathParts[1])) {
					handleGetHistory(resp, itemId);
				} else {
					sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
				}
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID format.");
			}
		}
	}

	private void handleGetStorageList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Map<String, List<StorageItem>> storageData = storageDAO.getAllItemsGroupedByLocation();
		List<Event> activeEvents = eventDAO.getActiveEvents();

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("storageData", storageData);
		responseData.put("activeEvents", activeEvents);

		sendJsonResponse(resp, HttpServletResponse.SC_OK,
				new ApiResponse(true, "Storage data retrieved.", responseData));
	}

	private void handleGetHistory(HttpServletResponse resp, int itemId) throws IOException {
		List<StorageLogEntry> transactionHistory = storageLogDAO.getHistoryForItem(itemId);
		List<MaintenanceLogEntry> maintenanceHistory = maintenanceLogDAO.getHistoryForItem(itemId);

		Map<String, Object> historyData = new HashMap<>();
		historyData.put("transactions", transactionHistory);
		historyData.put("maintenance", maintenanceHistory);

		sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "History retrieved.", historyData));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.equals("/transactions")) {
			handleTransaction(req, resp, user);
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	private void handleTransaction(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> payload = gson.fromJson(jsonPayload, type);

			int itemId = ((Double) payload.get("itemId")).intValue();
			int quantity = ((Double) payload.get("quantity")).intValue();
			String transactionType = (String) payload.get("type");
			String notes = (String) payload.get("notes");

			Integer eventId = null;
			if (payload.get("eventId") != null && !payload.get("eventId").toString().isEmpty()) {
				Object eventIdObj = payload.get("eventId");
				if (eventIdObj instanceof Double) {
					eventId = ((Double) eventIdObj).intValue();
				} else if (eventIdObj instanceof String) {
					eventId = Integer.parseInt((String) eventIdObj);
				}
				if (eventId != null && eventId == 0)
					eventId = null;
			}

			boolean success = storageService.processTransaction(itemId, quantity, transactionType, user, eventId,
					notes);
			if (success) {
				String action = "checkin".equals(transactionType) ? "eingeräumt" : "entnommen";
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Erfolgreich " + quantity + " Stück " + action + ".", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"Transaktion fehlgeschlagen. Grund: Nicht genügend Bestand oder Artikel ist bereits voll.");
			}
		} catch (Exception e) {
			logger.error("Error during storage transaction processing via API.", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
		}
	}

	private Integer parseId(String pathSegment) {
		try {
			return Integer.parseInt(pathSegment);
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