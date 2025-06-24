package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import de.technikteam.config.AppConfig;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 
 * Mapped to /admin/storage, this servlet provides full administrative control
 * 
 * over the inventory (storage_items table). It handles listing all items,
 * 
 * and processing the create, update, and delete actions (now via modals),
 * 
 * including associated image file uploads and deletions.
 */
@WebServlet("/admin/storage")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class AdminStorageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminStorageServlet.class.getName());
	private StorageDAO storageDAO;
	private Gson gson = new Gson();

	@Override
	public void init() {
		storageDAO = new StorageDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		if ("getItemData".equals(action)) {
			getItemDataAsJson(request, response);
			return;
		}

		try {
			logger.info("Listing all storage items for admin view.");
			Map<String, List<StorageItem>> groupedItems = storageDAO.getAllItemsGroupedByLocation();
			List<StorageItem> flatList = groupedItems.values().stream().flatMap(List::stream)
					.collect(Collectors.toList());
			request.setAttribute("storageList", flatList);
			request.getRequestDispatcher("/admin/admin_storage_list.jsp").forward(request, response);
		} catch (Exception e) {
			logger.error("Error in doGet of AdminStorageServlet", e);
			request.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/admin/dashboard");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		String contentType = request.getContentType();
		// Differentiate between multipart and standard forms
		if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
			String action = ServletUtils.getPartValue(request.getPart("action"));
			if ("create".equals(action)) {
				handleCreateOrUpdate(request, response, true);
			} else if ("update".equals(action)) {
				handleCreateOrUpdate(request, response, false);
			} else {
				logger.warn("Unknown multipart action received: {}", action);
				response.sendRedirect(request.getContextPath() + "/admin/storage");
			}
		} else {
			String action = request.getParameter("action");
			if ("delete".equals(action)) {
				handleDelete(request, response);
			} else {
				logger.warn("Unknown non-multipart action received: {}", action);
				response.sendRedirect(request.getContextPath() + "/admin/storage");
			}
		}
	}

	private void getItemDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int itemId = Integer.parseInt(req.getParameter("id"));
			StorageItem item = storageDAO.getItemById(itemId);
			if (item != null) {
				String itemJson = gson.toJson(item);
				resp.setContentType("application/json");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(itemJson);
			} else {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found");
			}
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response, boolean isCreate)
			throws IOException, ServletException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			StorageItem item = new StorageItem();
			item.setName(ServletUtils.getPartValue(request.getPart("name")));
			item.setLocation(ServletUtils.getPartValue(request.getPart("location")));
			item.setCabinet(ServletUtils.getPartValue(request.getPart("cabinet")));
			item.setShelf(ServletUtils.getPartValue(request.getPart("shelf")));
			item.setCompartment(ServletUtils.getPartValue(request.getPart("compartment")));
			item.setQuantity(Integer.parseInt(ServletUtils.getPartValue(request.getPart("quantity"))));
			item.setMaxQuantity(Integer.parseInt(ServletUtils.getPartValue(request.getPart("maxQuantity"))));
			logger.debug("SERVLET: Read from form -> Name: '{}', Quantity: {}, MaxQuantity: {}", item.getName(),
					item.getQuantity(), item.getMaxQuantity());
			Part filePart = request.getPart("imageFile");
			String imagePath = null;
			if (!isCreate) {
				int itemId = Integer.parseInt(ServletUtils.getPartValue(request.getPart("id")));
				item.setId(itemId);
				StorageItem originalItem = storageDAO.getItemById(itemId);
				if (originalItem != null) {
					imagePath = originalItem.getImagePath();
				}
			}

			if (filePart != null && filePart.getSize() > 0) {
				String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
				if (!fileName.isEmpty()) {
					File imageUploadDir = new File(AppConfig.UPLOAD_DIRECTORY, "images");
					if (!imageUploadDir.exists())
						imageUploadDir.mkdirs();
					File targetFile = new File(imageUploadDir, fileName);
					filePart.write(targetFile.getAbsolutePath());
					imagePath = fileName;
				}
			}
			item.setImagePath(imagePath);

			boolean success;
			if (isCreate) {
				success = storageDAO.createItem(item);
				if (success) {
					String logDetails = String.format(
							"Lagerartikel '%s' erstellt. Ort: %s, Schrank: %s, Anzahl: %d/%d.", item.getName(),
							item.getLocation(), item.getCabinet(), item.getQuantity(), item.getMaxQuantity());
					AdminLogService.log(adminUser.getUsername(), "CREATE_STORAGE_ITEM", logDetails);
					request.getSession().setAttribute("successMessage",
							"Artikel '" + item.getName() + "' erfolgreich erstellt.");
				}
			} else { // UPDATE
				StorageItem originalItem = storageDAO.getItemById(item.getId());
				success = storageDAO.updateItem(item);
				if (success) {
					String logDetails = String.format(
							"Lagerartikel '%s' (ID: %d) aktualisiert. Anzahl: %d -> %d, Ort: '%s' -> '%s'.",
							originalItem.getName(), item.getId(), originalItem.getQuantity(), item.getQuantity(),
							originalItem.getLocation(), item.getLocation());
					AdminLogService.log(adminUser.getUsername(), "UPDATE_STORAGE_ITEM", logDetails);
					request.getSession().setAttribute("successMessage",
							"Artikel '" + item.getName() + "' erfolgreich aktualisiert.");
				}
			}

			if (!success) {
				request.getSession().setAttribute("errorMessage",
						"Operation am Artikel fehlgeschlagen. Möglicherweise gab es keine Änderungen.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid number format for quantity or ID.", e);
			request.getSession().setAttribute("errorMessage", "Ungültiges Zahlenformat für Anzahl oder ID.");
		} catch (Exception e) {
			logger.error("Error creating/updating storage item.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/storage");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			StorageItem item = storageDAO.getItemById(itemId);
			if (item != null && item.getImagePath() != null && !item.getImagePath().isEmpty()) {
				// Correctly construct the path to the image directory
				File imageDir = new File(AppConfig.UPLOAD_DIRECTORY, "images");
				File imageFile = new File(imageDir, item.getImagePath());
				if (imageFile.exists()) {
					if (imageFile.delete()) {
						logger.info("Deleted physical image file: {}", imageFile.getAbsolutePath());
					} else {
						logger.warn("Could not delete physical image file: {}", imageFile.getAbsolutePath());
					}
				}
			}
			if (storageDAO.deleteItem(itemId)) {
				String itemName = (item != null) ? item.getName() : "N/A";
				String itemLocation = (item != null) ? item.getLocation() : "N/A";
				String logDetails = String.format("Lagerartikel '%s' (ID: %d) von Ort '%s' gelöscht.", itemName, itemId,
						itemLocation);
				AdminLogService.log(adminUser.getUsername(), "DELETE_STORAGE_ITEM", logDetails);
				request.getSession().setAttribute("successMessage", "Artikel erfolgreich gelöscht.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Artikel konnte nicht aus der Datenbank gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Artikel-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/storage");
	}

}