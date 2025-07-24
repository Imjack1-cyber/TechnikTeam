package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AdminReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminReportServlet.class);
	private final ReportDAO reportDAO;
	private final Gson gson = new Gson();

	@Inject
	public AdminReportServlet(ReportDAO reportDAO) {
		this.reportDAO = reportDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String reportType = request.getParameter("report");
		String exportType = request.getParameter("export");

		if (reportType != null && !reportType.isEmpty()) {
			handleSpecificReport(request, response, reportType, exportType);
			return;
		}

		List<Map<String, Object>> eventTrendData = reportDAO.getEventCountByMonth(12);
		List<Map<String, Object>> userActivityData = reportDAO.getUserParticipationStats(10);
		request.setAttribute("eventTrendDataJson", gson.toJson(eventTrendData));
		request.setAttribute("userActivityDataJson", gson.toJson(userActivityData));
		request.setAttribute("totalInventoryValue", reportDAO.getTotalInventoryValue());
		request.getRequestDispatcher("/views/admin/admin_reports.jsp").forward(request, response);
	}

	private void handleSpecificReport(HttpServletRequest request, HttpServletResponse response, String reportType,
			String exportType) throws IOException, ServletException {
		List<Map<String, Object>> reportData;
		String reportTitle;
		switch (reportType) {
		case "user_activity":
			reportData = reportDAO.getUserActivityStats();
			reportTitle = "Benutzeraktivitäts-Bericht";
			break;
		case "event_participation":
			reportData = reportDAO.getEventParticipationSummary();
			reportTitle = "Event-Teilnahme-Bericht";
			break;
		case "inventory_usage":
			reportData = reportDAO.getInventoryUsageFrequency();
			reportTitle = "Lagernutzungs-Bericht";
			break;
		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unbekannter Berichtstyp.");
			return;
		}

		if ("csv".equalsIgnoreCase(exportType)) {
			exportToCsv(response, reportData, reportType + "_report.csv");
		} else {
			request.setAttribute("reportData", reportData);
			request.setAttribute("reportTitle", reportTitle);
			request.getRequestDispatcher("/views/admin/report_display.jsp").forward(request, response);
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
}