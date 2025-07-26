// src/main/java/de/technikteam/api/v1/LogResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Singleton
public class LogResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LogResource.class);

	private final AdminLogDAO logDAO;
	private final Gson gson;

	@Inject
	public LogResource(AdminLogDAO logDAO, Gson gson) {
		this.logDAO = logDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("LOG_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String limitParam = req.getParameter("limit");
		try {
			List<AdminLog> logs;
			if (limitParam != null) {
				int limit = Integer.parseInt(limitParam);
				logs = logDAO.getRecentLogs(limit);
			} else {
				logs = logDAO.getAllLogs();
			}
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Logs retrieved successfully", logs));
		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid limit parameter.");
		} catch (Exception e) {
			logger.error("Error fetching admin logs", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred.");
		}
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}