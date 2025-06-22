package de.technikteam.servlet.admin;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.StatisticsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mapped to `/admin/dashboard`, this servlet serves as the entry point for the
 * main administrative dashboard. It uses the `StatisticsDAO` to fetch key
 * metrics like the total user count and the number of active events. It then
 * forwards this data to `admin_dashboard.jsp` for display.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminDashboardServlet.class);
	private StatisticsDAO statisticsDAO;

	@Override
	public void init() {
		statisticsDAO = new StatisticsDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Admin dashboard requested. Fetching statistics.");

		int userCount = statisticsDAO.getUserCount();
		int activeEventCount = statisticsDAO.getActiveEventCount();

		request.setAttribute("userCount", userCount);
		request.setAttribute("activeEventCount", activeEventCount);

		logger.debug("Forwarding to admin_dashboard.jsp with userCount={} and activeEventCount={}", userCount,
				activeEventCount);
		request.getRequestDispatcher("/admin/admin_dashboard.jsp").forward(request, response);
	}
}