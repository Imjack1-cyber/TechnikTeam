package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This servlet, mapped to `/lager`, is responsible for displaying the main
 * inventory/storage page for users. On a GET request, it fetches all storage
 * items from the database, grouped by their physical location, and forwards
 * this structured data to `/views/public/lager.jsp` for rendering.
 */
@WebServlet("/lager")
public class StorageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageServlet.class);
	private StorageDAO storageDAO;
	private EventDAO eventDAO;

	public void init() {
		storageDAO = new StorageDAO();
		eventDAO = new EventDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Main storage page requested. Fetching all items.");

		Map<String, List<StorageItem>> storageData = storageDAO.getAllItemsGroupedByLocation();
		List<Event> activeEvents = eventDAO.getActiveEvents();

		request.setAttribute("storageData", storageData);
		request.setAttribute("activeEvents", activeEvents);
		logger.debug("Forwarding {} location groups to /views/public/lager.jsp.", storageData.size());
		request.getRequestDispatcher("/views/public/lager.jsp").forward(request, response);
	}
}