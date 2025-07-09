package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mapped to `/admin/log`, this servlet retrieves all entries from the
 * administrative action log using the `AdminLogDAO`. It then passes the
 * complete list of logs to `admin_log.jsp` for display and filtering on the
 * client side.
 */
@WebServlet("/admin/log")
public class AdminLogServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminLogServlet.class);
	private AdminLogDAO adminLogDAO;

	@Override
	public void init() {
		adminLogDAO = new AdminLogDAO();
		logger.info("AdminLogServlet has been initialized.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		logger.info("Admin log page requested by user '{}'.", adminUser.getUsername());
		try {
			List<AdminLog> logs = adminLogDAO.getAllLogs();
			request.setAttribute("logs", logs);
			logger.info("Fetched {} log entries. Forwarding to JSP.", logs.size());
			request.getRequestDispatcher("/views/admin/admin_log.jsp").forward(request, response);
		} catch (Exception e) {
			logger.error("A critical error occurred in AdminLogServlet doGet()", e);
			response.sendRedirect(request.getContextPath() + "/error500");
		}
	}
}