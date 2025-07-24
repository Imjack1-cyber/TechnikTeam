package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.MaintenanceLogDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.MaintenanceLogEntry;
import de.technikteam.model.StorageItem;
import de.technikteam.model.StorageLogEntry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class StorageItemDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageItemDetailsServlet.class);
	private final StorageDAO storageDAO;
	private final StorageLogDAO storageLogDAO;
	private final MaintenanceLogDAO maintenanceLogDAO;

	@Inject
	public StorageItemDetailsServlet(StorageDAO storageDAO, StorageLogDAO storageLogDAO,
			MaintenanceLogDAO maintenanceLogDAO) {
		this.storageDAO = storageDAO;
		this.storageLogDAO = storageLogDAO;
		this.maintenanceLogDAO = maintenanceLogDAO;
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

			List<StorageLogEntry> history = storageLogDAO.getHistoryForItem(itemId);
			List<MaintenanceLogEntry> maintenanceHistory = maintenanceLogDAO.getHistoryForItem(itemId);

			request.setAttribute("item", item);
			request.setAttribute("history", history);
			request.setAttribute("maintenanceHistory", maintenanceHistory);

			logger.debug("Forwarding to storage_item_details.jsp for item '{}' with {} history entries.",
					item.getName(), history.size());
			request.getRequestDispatcher("/views/public/storage_item_details.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid storage item ID format in request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Artikel-ID.");
		}
	}
}