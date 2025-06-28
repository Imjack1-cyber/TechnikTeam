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
			Integer eventId = null;
			try {
				eventId = Integer.parseInt(request.getParameter("eventId"));
				if (eventId == 0)
					eventId = null; // Treat 0 as null
			} catch (NumberFormatException e) {
				// Ignore if not provided or invalid
			}

			int quantityChange = "checkin".equals(type) ? quantity : -quantity;
			logger.info("Processing storage transaction by user '{}': item ID {}, quantity change {}",
					user.getUsername(), itemId, quantityChange);

			boolean success = false;
			StorageItem item = storageDAO.getItemById(itemId);

			if (item == null) {
				throw new ServletException("Item with ID " + itemId + " not found.");
			}

			if ("checkout".equals(type)) {
				if (item.getAvailableQuantity() >= quantity) {
					success = storageDAO.updateItemQuantity(itemId, quantityChange);
					if (success) {
						storageDAO.updateItemHolderAndStatus(itemId, "CHECKED_OUT", user.getId(), eventId);
					}
				}
			} else if ("checkin".equals(type)) {
				// Allow check-in even if it exceeds max quantity, but ensure it's not a holder
				// anymore
				success = storageDAO.updateItemQuantity(itemId, quantityChange);
				if (success) {
					// Only change status if the current user is the holder
					if (item.getCurrentHolderUserId() == user.getId()) {
						storageDAO.updateItemHolderAndStatus(itemId, "IN_STORAGE", null, null);
					}
				}
			}

			if (success) {
				storageLogDAO.logTransaction(itemId, user.getId(), quantityChange, notes,
						eventId != null ? eventId : 0);

				String itemName = item.getName();
				String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
				String logDetails = String.format("%d x '%s' (ID: %d) %s. Notiz: %s", quantity, itemName, itemId,
						action, notes);
				AdminLogService.log(user.getUsername(), "STORAGE_TRANSACTION", logDetails);

				request.getSession().setAttribute("successMessage",
						"Erfolgreich " + quantity + " Stück " + action + ".");
			} else {
				logger.warn("Storage transaction failed for item ID {}. Not enough stock or other issue.", itemId);
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