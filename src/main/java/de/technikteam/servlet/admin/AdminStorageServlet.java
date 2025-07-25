package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.MaintenanceLogDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.MaintenanceLogEntry;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.ConfigurationService;
import de.technikteam.service.StorageService;
import de.technikteam.util.CSRFUtil;
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
import java.nio.file.Paths;
import java.util.List;

@Singleton
@MultipartConfig
public class AdminStorageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminStorageServlet.class);
	private final StorageDAO storageDAO;
	private final MaintenanceLogDAO maintenanceLogDAO;
	private final AdminLogService adminLogService;
	private final ConfigurationService configService;
	private final StorageService storageService;
	private final Gson gson = new Gson();

	@Inject
	public AdminStorageServlet(StorageDAO storageDAO, MaintenanceLogDAO maintenanceLogDAO,
			AdminLogService adminLogService, ConfigurationService configService, StorageService storageService) {
		this.storageDAO = storageDAO;
		this.maintenanceLogDAO = maintenanceLogDAO;
		this.adminLogService = adminLogService;
		this.configService = configService;
		this.storageService = storageService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		if ("getItemData".equals(action)) {
			getItemDataAsJson(request, response);
			return;
		}

		List<StorageItem> storageList = storageDAO.getAllItems();
		request.setAttribute("storageList", storageList);
		request.getRequestDispatcher("/views/admin/admin_storage_list.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = request.getParameter("action");

		switch (action) {
		case "create":
		case "update":
			handleCreateOrUpdate(request, response);
			break;
		case "delete":
			handleDelete(request, response);
			break;
		case "updateDefectStatus":
			handleDefectStatusUpdate(request, response);
			break;
		case "updateStatus":
			handleStatusUpdate(request, response);
			break;
		case "repair":
			handleRepair(request, response);
			break;
		default:
			response.sendRedirect(request.getContextPath() + "/admin/lager");
			break;
		}
	}

	private void getItemDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int itemId = Integer.parseInt(req.getParameter("id"));
			StorageItem item = storageDAO.getItemById(itemId);
			if (item != null) {
				resp.setContentType("application/json");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(gson.toJson(item));
			} else {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found");
			}
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		User adminUser = (User) request.getSession().getAttribute("user");
		boolean isCreate = "create".equals(request.getParameter("action"));

		try {
			StorageItem item = new StorageItem();
			item.setName(request.getParameter("name"));
			item.setLocation(request.getParameter("location"));
			item.setCabinet(request.getParameter("cabinet"));
			item.setCompartment(request.getParameter("compartment"));
			item.setQuantity(Integer.parseInt(request.getParameter("quantity")));
			item.setMaxQuantity(Integer.parseInt(request.getParameter("maxQuantity")));
			String weightStr = request.getParameter("weight_kg");
			item.setWeightKg(
					weightStr == null || weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr.replace(',', '.')));
			String priceStr = request.getParameter("price_eur");
			item.setPriceEur(
					priceStr == null || priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr.replace(',', '.')));

			Part filePart = request.getPart("imageFile");
			String imagePath = null;

			if (!isCreate) {
				int itemId = Integer.parseInt(request.getParameter("id"));
				item.setId(itemId);
				StorageItem originalItem = storageDAO.getItemById(itemId);
				if (originalItem != null) {
					imagePath = originalItem.getImagePath();
					item.setDefectiveQuantity(originalItem.getDefectiveQuantity());
					item.setDefectReason(originalItem.getDefectReason());
					item.setStatus(originalItem.getStatus());
				}
			}

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

			boolean success = isCreate ? storageDAO.createItem(item) : storageDAO.updateItem(item);

			if (success) {
				String logDetails = String.format("Lagerartikel '%s' %s.", item.getName(),
						isCreate ? "erstellt" : "aktualisiert");
				adminLogService.log(adminUser.getUsername(), isCreate ? "CREATE_STORAGE_ITEM" : "UPDATE_STORAGE_ITEM",
						logDetails);
				request.getSession().setAttribute("successMessage",
						"Artikel '" + item.getName() + "' erfolgreich gespeichert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Operation am Artikel fehlgeschlagen.");
			}
		} catch (Exception e) {
			logger.error("Error creating/updating storage item.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/lager");
	}

	private void handleDefectStatusUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			String status = request.getParameter("status"); // "DEFECT" or "UNREPAIRABLE"
			int quantity = Integer.parseInt(request.getParameter("quantity"));
			String reason = request.getParameter("reason");

			boolean success = storageService.updateDefectiveItemStatus(itemId, status, quantity, reason, adminUser);

			if (success) {
				request.getSession().setAttribute("successMessage", "Defekt-Status erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Status konnte nicht aktualisiert werden. Überprüfen Sie die Bestandsmengen.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Artikel-ID oder Anzahl.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/lager");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			StorageItem item = storageDAO.getItemById(itemId);
			if (item != null && item.getImagePath() != null && !item.getImagePath().isEmpty()) {
				File imageDir = new File(configService.getProperty("upload.directory"), "images");
				File imageFile = new File(imageDir, item.getImagePath());
				if (imageFile.exists() && !imageFile.delete()) {
					logger.warn("Could not delete physical image file: {}", imageFile.getAbsolutePath());
				}
			}
			if (storageDAO.deleteItem(itemId)) {
				adminLogService.log(adminUser.getUsername(), "DELETE_STORAGE_ITEM", String.format(
						"Lagerartikel '%s' (ID: %d) gelöscht.", (item != null ? item.getName() : "N/A"), itemId));
				request.getSession().setAttribute("successMessage", "Artikel erfolgreich gelöscht.");
			} else {
				request.getSession().setAttribute("errorMessage", "Artikel konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Artikel-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/lager");
	}

	private void handleStatusUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			String newStatus = request.getParameter("status");
			String notes = request.getParameter("notes");
			storageDAO.updateItemStatus(itemId, newStatus);
			MaintenanceLogEntry log = new MaintenanceLogEntry();
			log.setItemId(itemId);
			log.setUserId(adminUser.getId());
			log.setNotes(notes);
			String logAction = "MAINTENANCE".equals(newStatus) ? "Marked for Maintenance" : "Returned to Service";
			log.setAction(logAction);
			maintenanceLogDAO.createLog(log);
			adminLogService.log(adminUser.getUsername(), "UPDATE_ITEM_STATUS",
					"Status für Artikel-ID " + itemId + " auf '" + newStatus + "' gesetzt. Notiz: " + notes);
			request.getSession().setAttribute("successMessage", "Artikelstatus erfolgreich aktualisiert.");
		} catch (Exception e) {
			logger.error("Error updating item status", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren des Status.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/lager");
	}

	private void handleRepair(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		String returnTo = request.getParameter("returnTo");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			int repairedQty = Integer.parseInt(request.getParameter("repaired_quantity"));
			String notes = request.getParameter("repair_notes");
			if (storageDAO.repairItems(itemId, repairedQty)) {
				MaintenanceLogEntry log = new MaintenanceLogEntry();
				log.setItemId(itemId);
				log.setUserId(adminUser.getId());
				log.setAction(repairedQty + " Stück repariert");
				log.setNotes(notes);
				maintenanceLogDAO.createLog(log);
				adminLogService.log(adminUser.getUsername(), "REPAIR_ITEM", String.format(
						"%d Stück von Artikel-ID %d als repariert markiert. Notiz: %s", repairedQty, itemId, notes));
				request.getSession().setAttribute("successMessage", "Artikel erfolgreich als repariert markiert.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Reparatur konnte nicht verbucht werden (vielleicht nicht genug defekte Artikel?).");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Artikel-ID oder Anzahl.");
		}
		String redirectUrl = request.getContextPath()
				+ ("/defekte".equals(returnTo) ? "/admin/defekte" : "/admin/lager");
		response.sendRedirect(redirectUrl);
	}
}