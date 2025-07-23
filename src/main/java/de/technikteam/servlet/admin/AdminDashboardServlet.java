package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.StatisticsDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mapped to `/admin/dashboard`, this servlet serves as the entry point for the
 * main administrative dashboard. It simply forwards to the JSP, which then uses
 * JavaScript to fetch its data dynamically.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminDashboardServlet.class);

	@Override
	public void init() {
		// DAOs are no longer needed here as data is fetched via API.
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Admin dashboard page requested. Forwarding to JSP for dynamic data loading.");

		request.getRequestDispatcher("/views/admin/admin_dashboard.jsp").forward(request, response);
	}
}