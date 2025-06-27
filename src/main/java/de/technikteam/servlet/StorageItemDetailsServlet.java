package de.technikteam.servlet;

import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.StorageItem;
import de.technikteam.model.StorageLogEntry;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * REDESIGN: Mapped to `/storage-item`, this servlet now displays a
 * comprehensive, public-facing detail page for a single inventory item. It
 * fetches the item's core data AND its full transaction history ("chronic"),
 * forwarding both to `storage_item_details.jsp` for rendering a unified view.
 */
@WebServlet("/storage-item")
public class StorageItemDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageItemDetailsServlet.class);
	private StorageDAO storageDAO;
	private StorageLogDAO storageLogDAO; // Added for fetching history

	@Override
	public void init() {
		storageDAO = new StorageDAO();
		storageLogDAO = new StorageLogDAO(); // Initialize the log DAO
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			logger.info("Comprehensive storage item details requested for ID: {}", itemId);

			StorageItem item = storageDAO.getItemById(itemId);

			if (item == null) {
				logger.warn("Storage item with ID {} not found.", itemId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Artikel nicht gefunden.");
				return;
			}

			// Fetch the transaction history for the item
			List<StorageLogEntry> history = storageLogDAO.getHistoryForItem(itemId);

			request.setAttribute("item", item);
			request.setAttribute("history", history); // Pass history to the JSP

			logger.debug("Forwarding to storage_item_details.jsp for item '{}' with {} history entries.",
					item.getName(), history.size());
			request.getRequestDispatcher("/storage_item_details.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid storage item ID format in request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Artikel-ID.");
		}
	}
}