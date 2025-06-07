package de.technikteam.servlet.admin;

import java.io.File; // Wichtig: java.io.File
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;

@WebServlet("/admin/storage")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1 MB
		maxFileSize = 1024 * 1024 * 5, // 5 MB
		maxRequestSize = 1024 * 1024 * 10 // 10 MB
)
public class AdminStorageServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(AdminStorageServlet.class);
	private StorageDAO storageDAO;

	@Override
	public void init() {
		storageDAO = new StorageDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		if (action == null) {
			action = "list"; // Default
		}

		switch (action) {
		case "edit":
		case "new":
			showForm(request, response);
			break;
		case "list":
		default:
			listItems(request, response);
			break;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		if (action == null) {
			response.sendRedirect(request.getContextPath() + "/admin/storage");
			return;
		}

		switch (action) {
		case "create":
			handleCreateOrUpdate(request, response, true);
			break;
		case "update":
			handleCreateOrUpdate(request, response, false);
			break;
		case "delete":
			handleDelete(request, response);
			break;
		default:
			response.sendRedirect(request.getContextPath() + "/admin/storage");
			break;
		}
	}

	private void listItems(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, List<StorageItem>> groupedItems = storageDAO.getAllItemsGroupedByLocation();
		List<StorageItem> flatList = groupedItems.values().stream().flatMap(List::stream).collect(Collectors.toList());
		request.setAttribute("storageList", flatList);
		request.getRequestDispatcher("/admin/admin_storage_list.jsp").forward(request, response);
	}

	private void showForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if ("edit".equals(request.getParameter("action"))) {
			try {
				int itemId = Integer.parseInt(request.getParameter("id"));
				StorageItem item = storageDAO.getItemById(itemId);
				request.setAttribute("storageItem", item);
			} catch (NumberFormatException e) {
				logger.error("Invalid storage item ID for edit.", e);
			}
		}
		request.getRequestDispatcher("/admin/admin_storage_form.jsp").forward(request, response);
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response, boolean isCreate)
			throws IOException, ServletException {
		try {
			StorageItem item = new StorageItem();
			item.setName(request.getParameter("name"));
			item.setLocation(request.getParameter("location"));
			item.setCabinet(request.getParameter("cabinet"));
			item.setShelf(request.getParameter("shelf"));
			item.setCompartment(request.getParameter("compartment"));
			item.setQuantity(Integer.parseInt(request.getParameter("quantity")));

			// Image Upload Logic
			Part filePart = request.getPart("imageFile");
			String imagePath = request.getParameter("imagePath"); // Keep old path as fallback

			if (filePart != null && filePart.getSize() > 0) {
				String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
				if (!fileName.isEmpty()) {
					String applicationPath = request.getServletContext().getRealPath("");
					String uploadDir = applicationPath + File.separator + "images";

					File uploadDirFile = new File(uploadDir);
					if (!uploadDirFile.exists())
						uploadDirFile.mkdirs();

					filePart.write(uploadDir + File.separator + fileName);
					imagePath = "images/" + fileName; // Set new path for the database
				}
			}
			item.setImagePath(imagePath);

			boolean success;
			if (isCreate) {
				success = storageDAO.createItem(item);
				request.getSession().setAttribute("successMessage",
						"Artikel '" + item.getName() + "' erfolgreich erstellt.");
			} else {
				item.setId(Integer.parseInt(request.getParameter("id")));
				success = storageDAO.updateItem(item);
				request.getSession().setAttribute("successMessage",
						"Artikel '" + item.getName() + "' erfolgreich aktualisiert.");
			}
			if (!success) {
				request.getSession().setAttribute("errorMessage", "Operation am Artikel fehlgeschlagen.");
			}
		} catch (NumberFormatException | IOException | ServletException e) {
			logger.error("Error creating/updating storage item.", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Verarbeiten der Anfrage: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/storage");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			if (storageDAO.deleteItem(itemId)) {
				request.getSession().setAttribute("successMessage", "Artikel erfolgreich gelöscht.");
			} else {
				request.getSession().setAttribute("errorMessage", "Artikel konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Artikel-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/storage");
	}
}