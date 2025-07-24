package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import de.technikteam.service.StorageService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Singleton
public class StorageTransactionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageTransactionServlet.class);
	private final StorageService storageService;

	@Inject
	public StorageTransactionServlet(StorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for storage transaction by user '{}'",
					user != null ? user.getUsername() : "GUEST");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String redirectUrl = request.getParameter("redirectUrl");
		if (redirectUrl == null || redirectUrl.isEmpty()) {
			redirectUrl = request.getContextPath() + "/lager";
		}

		try {
			int itemId = Integer.parseInt(request.getParameter("itemId"));
			int quantity = Integer.parseInt(request.getParameter("quantity"));
			String type = request.getParameter("type");
			String notes = request.getParameter("notes");
			Integer eventId = null;
			String eventIdParam = request.getParameter("eventId");
			if (eventIdParam != null && !eventIdParam.isEmpty()) {
				eventId = Integer.parseInt(eventIdParam);
				if (eventId == 0)
					eventId = null;
			}

			boolean success = storageService.processTransaction(itemId, quantity, type, user, eventId, notes);

			if (success) {
				String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
				request.getSession().setAttribute("successMessage",
						"Erfolgreich " + quantity + " Stück " + action + ".");
			} else {
				if (request.getSession().getAttribute("errorMessage") == null) {
					request.getSession().setAttribute("errorMessage",
							"Transaktion fehlgeschlagen. Grund: Nicht genügend Bestand oder Artikel ist bereits voll.");
				}
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid number format in storage transaction request.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültiges Zahlenformat.");
		} catch (Exception e) {
			logger.error("Error during storage transaction processing.", e);
			request.getSession().setAttribute("errorMessage",
					"Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
		}

		response.sendRedirect(redirectUrl);
	}
}