package de.technikteam.servlet.admin;

import java.io.IOException;
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Singleton
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