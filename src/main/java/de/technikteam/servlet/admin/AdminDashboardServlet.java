// Modify src/main/java/de/technikteam/servlet/admin/AdminDashboardServlet.java
package de.technikteam.servlet.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.StatisticsDAO; // Import new DAO

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminDashboardServlet.class);
	private StatisticsDAO statisticsDAO;

	@Override
	public void init() {
		statisticsDAO = new StatisticsDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("Admin dashboard requested. Fetching statistics.");

		int userCount = statisticsDAO.getUserCount();
		int activeEventCount = statisticsDAO.getActiveEventCount();

		request.setAttribute("userCount", userCount);
		request.setAttribute("activeEventCount", activeEventCount);

		request.getRequestDispatcher("/admin/admin_dashboard.jsp").forward(request, response);
	}
}