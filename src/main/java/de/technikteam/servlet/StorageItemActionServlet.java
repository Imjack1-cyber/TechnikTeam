package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Singleton
public class StorageItemActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final StorageDAO storageDAO;
	private final EventDAO eventDAO;

	@Inject
	public StorageItemActionServlet(StorageDAO storageDAO, EventDAO eventDAO) {
		this.storageDAO = storageDAO;
		this.eventDAO = eventDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int itemId = Integer.parseInt(request.getParameter("id"));
			StorageItem item = storageDAO.getItemById(itemId);
			List<Event> activeEvents = eventDAO.getActiveEvents();

			if (item == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Artikel nicht gefunden.");
				return;
			}
			request.setAttribute("item", item);
			request.setAttribute("activeEvents", activeEvents);
			request.getRequestDispatcher("/views/public/qr_action.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Artikel-ID.");
		}
	}
}