package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet; // Sicherstellen, dass dieser Import da ist
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;

// Stellen Sie sicher, dass diese Annotation exakt so da ist
//In AdminLogServlet.java
@WebServlet("/admin/log")
public class AdminLogServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminLogServlet.class);
	private AdminLogDAO adminLogDAO;

	@Override
	public void init() {
		adminLogDAO = new AdminLogDAO();
		// Diese Nachricht MUSS beim Serverstart in der catalina.log erscheinen.
		logger.info("===================================================");
		logger.info("      AdminLogServlet has been INITIALIZED.      ");
		logger.info("===================================================");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info(">>>> AdminLogServlet doGet() method ENTERED. <<<<");
		try {
			List<AdminLog> logs = adminLogDAO.getAllLogs();
			request.setAttribute("logs", logs);
			logger.info("Fetched {} log entries. Forwarding to JSP...", logs.size());
			request.getRequestDispatcher("/admin/admin_log.jsp").forward(request, response);
		} catch (Exception e) {
			logger.error("FATAL ERROR in AdminLogServlet doGet()", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}