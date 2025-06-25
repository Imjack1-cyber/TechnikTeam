package de.technikteam.servlet;

import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Mapped to `/storage-transaction`, this servlet handles the business logic for
 * checking items in and out of the inventory. It processes POST requests from
 * the modal on the main storage page. It atomically updates the item quantity
 * in the `storage_items` table and creates a record of the transaction in both
 * the `storage_log` table and the main administrative audit log.
 */
@WebServlet("/storage-transaction")
public class StorageTransactionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageTransactionServlet.class);
	private StorageDAO storageDAO;
	private StorageLogDAO storageLogDAO;

	@Override
	public void init() {
		storageDAO = new StorageDAO();
		storageLogDAO = new StorageLogDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String redirectUrl = request.getParameter("redirectUrl");
		if (redirectUrl == null || redirectUrl.isEmpty()) {
			redirectUrl = request.getContextPath() + "/lager";
		}

		try {
			int itemId = Integer.parseInt(request.getParameter("itemId"));
			int quantity = Integer.parseInt(request.getParameter("quantity"));
			String type = request.getParameter("type"); // "checkout" or "checkin"
			String notes = request.getParameter("notes");
			int eventId = 0;
			try {
				eventId = Integer.parseInt(request.getParameter("eventId"));
			} catch (NumberFormatException e) {
				// Ignore if not provided or invalid
			}

			int quantityChange = "checkin".equals(type) ? quantity : -quantity;
			logger.info("Processing storage transaction by user '{}': item ID {}, quantity change {}",
					user.getUsername(), itemId, quantityChange);

			// Atomically update the quantity. This can fail if there's not enough stock.
			boolean success = storageDAO.updateItemQuantity(itemId, quantityChange);

			if (success) {
				// Log the transaction in the specific storage log and the general admin log.
				storageLogDAO.logTransaction(itemId, user.getId(), quantityChange, notes, eventId);

				StorageItem item = storageDAO.getItemById(itemId);
				String itemName = (item != null) ? item.getName() : "N/A";
				String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
				String logDetails = String.format("%d x '%s' (ID: %d) %s. Notiz: %s", quantity, itemName, itemId,
						action, notes);
				AdminLogService.log(user.getUsername(), "STORAGE_TRANSACTION", logDetails);

				request.getSession().setAttribute("successMessage",
						"Erfolgreich " + quantity + " Stück " + action + ".");
			} else {
				logger.warn("Storage transaction failed for item ID {}. Not enough stock.", itemId);
				request.getSession().setAttribute("errorMessage",
						"Transaktion fehlgeschlagen. Nicht genügend Artikel auf Lager?");
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid number format in storage transaction request.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültiges Zahlenformat.");
		} catch (SQLException e) {
			logger.error("SQL error during storage transaction.", e);
			request.getSession().setAttribute("errorMessage", "Datenbankfehler bei der Transaktion: " + e.getMessage());
		}

		response.sendRedirect(redirectUrl);
	}
}