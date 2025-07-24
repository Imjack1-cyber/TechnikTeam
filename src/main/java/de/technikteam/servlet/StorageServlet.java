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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Singleton
public class StorageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StorageServlet.class);
	private final StorageDAO storageDAO;
	private final EventDAO eventDAO;

	@Inject
	public StorageServlet(StorageDAO storageDAO, EventDAO eventDAO) {
		this.storageDAO = storageDAO;
		this.eventDAO = eventDAO;
	}

	@Override
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