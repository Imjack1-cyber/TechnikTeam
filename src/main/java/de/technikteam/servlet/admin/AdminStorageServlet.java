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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/lager")
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
			// CORRECTED: Fetch a flat List of all items, which is what the
			// admin_storage_list.jsp expects.
			List<StorageItem> storageList = storageDAO.getAllItems();
			// CORRECTED: Set the attribute with the name "storageList" to match the JSP's
			// <c:forEach> tag.
			request.setAttribute("storageList", storageList);
			request.getRequestDispatcher("/views/admin/admin_storage_list.jsp").forward(request, response);
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
		String action;

		if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
			action = ServletUtils.getPartValue(request.getPart("action"));
		} else {
			action = request.getParameter("action");
		}

		switch (action) {
		case "create":
		case "update":
			handleCreateOrUpdate(request, response);
			break;
		case "delete":
			handleDelete(request, response);
			break;
		case "updateDefect":
			handleDefectUpdate(request, response);
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
		boolean isCreate = "create".equals(ServletUtils.getPartValue(request.getPart("action")));

		try {
			StorageItem item = new StorageItem();
			item.setName(ServletUtils.getPartValue(request.getPart("name")));
			item.setLocation(ServletUtils.getPartValue(request.getPart("location")));
			item.setCabinet(ServletUtils.getPartValue(request.getPart("cabinet")));
			item.setCompartment(ServletUtils.getPartValue(request.getPart("compartment")));
			item.setQuantity(Integer.parseInt(ServletUtils.getPartValue(request.getPart("quantity"))));
			item.setMaxQuantity(Integer.parseInt(ServletUtils.getPartValue(request.getPart("maxQuantity"))));

			String weightStr = ServletUtils.getPartValue(request.getPart("weight_kg"));
			item.setWeightKg(
					weightStr == null || weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr.replace(',', '.')));
			String priceStr = ServletUtils.getPartValue(request.getPart("price_eur"));
			item.setPriceEur(
					priceStr == null || priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr.replace(',', '.')));

			Part filePart = request.getPart("imageFile");
			String imagePath = null;

			if (!isCreate) {
				int itemId = Integer.parseInt(ServletUtils.getPartValue(request.getPart("id")));
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
					File imageUploadDir = new File(AppConfig.UPLOAD_DIRECTORY, "images");
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
				AdminLogService.log(adminUser.getUsername(), isCreate ? "CREATE_STORAGE_ITEM" : "UPDATE_STORAGE_ITEM",
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

	private void handleDefectUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		String returnTo = request.getParameter("returnTo");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			int defectiveQty = Integer.parseInt(request.getParameter("defective_quantity"));
			String reason = request.getParameter("defect_reason");

			if (storageDAO.updateDefectiveStatus(itemId, defectiveQty, reason)) {
				AdminLogService.log(adminUser.getUsername(), "UPDATE_DEFECT_STATUS",
						String.format("Defekt-Status für Artikel-ID %d aktualisiert: %d defekt. Grund: %s", itemId,
								defectiveQty, reason));
				request.getSession().setAttribute("successMessage", "Defekt-Status aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Defekt-Status konnte nicht aktualisiert werden (vielleicht nicht genug Bestand?).");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Artikel-ID oder Anzahl.");
		} catch (SQLException e) {
			request.getSession().setAttribute("errorMessage", "Datenbankfehler: " + e.getMessage());
		}

		String redirectUrl = request.getContextPath()
				+ ("/defekte".equals(returnTo) ? "/admin/defekte" : "/admin/lager");
		response.sendRedirect(redirectUrl);
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			StorageItem item = storageDAO.getItemById(itemId);
			if (item != null && item.getImagePath() != null && !item.getImagePath().isEmpty()) {
				File imageDir = new File(AppConfig.UPLOAD_DIRECTORY, "images");
				File imageFile = new File(imageDir, item.getImagePath());
				if (imageFile.exists() && !imageFile.delete()) {
					logger.warn("Could not delete physical image file: {}", imageFile.getAbsolutePath());
				}
			}
			if (storageDAO.deleteItem(itemId)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_STORAGE_ITEM", String.format(
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
}