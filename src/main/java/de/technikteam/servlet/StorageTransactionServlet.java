package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.Event;
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

@WebServlet("/lager/transaktion")
public class StorageTransactionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageTransactionServlet.class);
	private StorageDAO storageDAO;
	private StorageLogDAO storageLogDAO;
	private EventDAO eventDAO;

	@Override
	public void init() {
		storageDAO = new StorageDAO();
		storageLogDAO = new StorageLogDAO();
		eventDAO = new EventDAO();
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
				String eventIdParam = request.getParameter("eventId");
				if (eventIdParam != null && !eventIdParam.isEmpty()) {
					eventId = Integer.parseInt(eventIdParam);
					if (eventId == 0)
						eventId = null;
				}
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
				if (item.getAvailableQuantity() < quantity) {
					request.getSession().setAttribute("errorMessage",
							"Entnahme fehlgeschlagen: Nicht genügend Artikel verfügbar.");
				} else {
					success = storageDAO.performCheckout(itemId, quantity, user.getId(), eventId);
				}
			} else if ("checkin".equals(type)) {
				// FIX: Correctly check against max quantity BEFORE attempting the transaction.
				if (item.getMaxQuantity() > 0 && (item.getQuantity() + quantity > item.getMaxQuantity())) {
					int availableSpace = item.getMaxQuantity() - item.getQuantity();
					if (availableSpace > 0) {
						request.getSession().setAttribute("errorMessage",
								"Einräumen fehlgeschlagen: Es ist nur Platz für " + availableSpace
										+ " weitere Artikel.");
					} else {
						request.getSession().setAttribute("errorMessage",
								"Einräumen fehlgeschlagen: Das Lager für diesen Artikel ist bereits voll.");
					}
				} else {
					success = storageDAO.performCheckin(itemId, quantity);
				}
			}

			if (success) {
				String finalNotes = notes;
				if ("checkout".equals(type) && eventId != null) {
					Event event = eventDAO.getEventById(eventId);
					if (event != null) {
						String autoNote = "Für Event: " + event.getName();
						finalNotes = (notes != null && !notes.trim().isEmpty()) ? autoNote + " - " + notes : autoNote;
					}
				}

				storageLogDAO.logTransaction(itemId, user.getId(), quantityChange, finalNotes,
						eventId != null ? eventId : 0);

				String itemName = item.getName();
				String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
				String logDetails = String.format("%d x '%s' (ID: %d) %s. Notiz: %s", quantity, itemName, itemId,
						action, finalNotes);
				AdminLogService.log(user.getUsername(), "STORAGE_TRANSACTION", logDetails);

				request.getSession().setAttribute("successMessage",
						"Erfolgreich " + quantity + " Stück " + action + ".");
			} else {
				if (request.getSession().getAttribute("errorMessage") == null) {
					request.getSession().setAttribute("errorMessage",
							"Transaktion fehlgeschlagen. Bitte erneut versuchen.");
				}
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