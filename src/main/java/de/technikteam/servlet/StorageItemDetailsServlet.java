package de.technikteam.servlet;

import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Mapped to `/storage-item`, this servlet displays a public-facing detail page
 * for a single inventory item. It is typically accessed by scanning a QR code
 * that contains the URL with the item's ID. It fetches the item's data using
 * `StorageDAO` and forwards it to `storage_item_details.jsp` for rendering.
 */
@WebServlet("/storage-item")
public class StorageItemDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageItemDetailsServlet.class);
	private StorageDAO storageDAO;

	@Override
	public void init() {
		storageDAO = new StorageDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			logger.info("Storage item details requested for ID: {}", itemId);

			StorageItem item = storageDAO.getItemById(itemId);

			if (item == null) {
				logger.warn("Storage item with ID {} not found.", itemId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Artikel nicht gefunden.");
				return;
			}

			request.setAttribute("item", item);
			logger.debug("Forwarding to storage_item_details.jsp for item '{}'", item.getName());
			request.getRequestDispatcher("/storage_item_details.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid storage item ID format in request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Artikel-ID.");
		}
	}
}