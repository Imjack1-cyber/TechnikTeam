package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.StatisticsDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class AdminDashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminDashboardServlet.class);
	private final StorageDAO storageDAO;
	private final StatisticsDAO statisticsDAO;

	@Inject
	public AdminDashboardServlet(StorageDAO storageDAO, StatisticsDAO statisticsDAO) {
		this.storageDAO = storageDAO;
		this.statisticsDAO = statisticsDAO;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Admin dashboard page requested. Fetching initial widget data.");

		List<StorageItem> defectiveItems = storageDAO.getDefectiveItems();
		int userCount = statisticsDAO.getUserCount();
		int activeEventCount = statisticsDAO.getActiveEventCount();

		request.setAttribute("defectiveItems", defectiveItems);
		request.setAttribute("userCount", userCount);
		request.setAttribute("activeEventCount", activeEventCount);

		request.getRequestDispatcher("/views/admin/admin_dashboard.jsp").forward(request, response);
	}
}