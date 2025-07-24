package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class AdminLogServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminLogServlet.class);
	private final AdminLogDAO adminLogDAO;

	@Inject
	public AdminLogServlet(AdminLogDAO adminLogDAO) {
		this.adminLogDAO = adminLogDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (adminUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		List<AdminLog> logs = adminLogDAO.getAllLogs();
		request.setAttribute("logs", logs);
		request.getRequestDispatcher("/views/admin/admin_log.jsp").forward(request, response);
	}
}