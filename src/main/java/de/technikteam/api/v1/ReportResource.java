// src/main/java/de/technikteam/api/v1/ReportResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.ReportDAO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ReportResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ReportResource.class);

	private final ReportDAO reportDAO;
	private final Gson gson;

	@Inject
	public ReportResource(ReportDAO reportDAO, Gson gson) {
		this.reportDAO = reportDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("REPORT_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
					"Please specify a report type (e.g., /dashboard, /user-activity).");
			return;
		}

		String reportType = pathInfo.substring(1);
		List<Map<String, Object>> reportData;

		switch (reportType) {
		case "dashboard":
			Map<String, Object> dashboardData = new HashMap<>();
			dashboardData.put("eventTrend", reportDAO.getEventCountByMonth(12));
			dashboardData.put("userActivity", reportDAO.getUserParticipationStats(10));
			dashboardData.put("totalInventoryValue", reportDAO.getTotalInventoryValue());
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Dashboard data retrieved", dashboardData));
			return;
		case "user-activity":
			reportData = reportDAO.getUserActivityStats();
			break;
		case "event-participation":
			reportData = reportDAO.getEventParticipationSummary();
			break;
		case "inventory-usage":
			reportData = reportDAO.getInventoryUsageFrequency();
			break;
		default:
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown report type.");
			return;
		}

		String acceptHeader = req.getHeader("Accept");
		if (acceptHeader != null && acceptHeader.contains("text/csv")) {
			exportToCsv(resp, reportData, reportType + "_report.csv");
		} else {
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Report data retrieved", reportData));
		}
	}

	private void exportToCsv(HttpServletResponse response, List<Map<String, Object>> data, String filename)
			throws IOException {
		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		if (data == null || data.isEmpty()) {
			response.getWriter().write("No data available to export.");
			return;
		}
		try (PrintWriter writer = response.getWriter()) {
			String header = String.join(",", data.get(0).keySet());
			writer.println(header);
			for (Map<String, Object> row : data) {
				String line = row.values().stream().map(this::escapeCsvField).collect(Collectors.joining(","));
				writer.println(line);
			}
		}
	}

	private String escapeCsvField(Object field) {
		if (field == null)
			return "";
		String fieldStr = field.toString();
		if (fieldStr.contains(",") || fieldStr.contains("\"") || fieldStr.contains("\n")) {
			return "\"" + fieldStr.replace("\"", "\"\"") + "\"";
		}
		return fieldStr;
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