package de.technikteam.servlet.admin;

import de.technikteam.dao.ReportDAO;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
import java.util.stream.Stream;

@WebServlet("/admin/reports")
public class AdminReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminReportServlet.class);
	private ReportDAO reportDAO;

	@Override
	public void init() {
		reportDAO = new ReportDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String reportType = request.getParameter("report");
		String exportType = request.getParameter("export");

		if (reportType == null) {
			logger.debug("Serving main reports menu.");
			request.setAttribute("totalInventoryValue", reportDAO.getTotalInventoryValue());
			request.getRequestDispatcher("/admin/admin_reports.jsp").forward(request, response);
			return;
		}

		List<Map<String, Object>> reportData = null;
		String reportTitle = "";
		String jspPath = "";

		switch (reportType) {
		case "user_activity":
			reportData = reportDAO.getUserActivityStats();
			reportTitle = "Benutzeraktivitäts-Bericht";
			jspPath = "/admin/report_display.jsp";
			break;
		case "event_participation":
			reportData = reportDAO.getEventParticipationSummary();
			reportTitle = "Event-Teilnahme-Bericht";
			jspPath = "/admin/report_display.jsp";
			break;
		case "inventory_usage":
			reportData = reportDAO.getInventoryUsageFrequency();
			reportTitle = "Lagernutzungs-Bericht";
			jspPath = "/admin/report_display.jsp";
			break;
		default:
			logger.warn("Unknown report type requested: {}", reportType);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unbekannter Berichtstyp.");
			return;
		}

		if ("csv".equalsIgnoreCase(exportType)) {
			logger.info("Exporting report '{}' to CSV.", reportType);
			exportToCsv(response, reportData, reportType + "_report.csv");
		} else {
			logger.debug("Forwarding data for report '{}' to JSP.", reportType);
			request.setAttribute("reportData", reportData);
			request.setAttribute("reportTitle", reportTitle);
			request.getRequestDispatcher(jspPath).forward(request, response);
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
			// Write header
			String header = String.join(",", data.get(0).keySet());
			writer.println(header);

			// Write data rows
			for (Map<String, Object> row : data) {
				String line = row.values().stream().map(this::escapeCsvField).collect(Collectors.joining(","));
				writer.println(line);
			}
		}
	}

	private String escapeCsvField(Object field) {
		if (field == null) {
			return "";
		}
		String fieldStr = field.toString();
		if (fieldStr.contains(",") || fieldStr.contains("\"") || fieldStr.contains("\n")) {
			return "\"" + fieldStr.replace("\"", "\"\"") + "\"";
		}
		return fieldStr;
	}
}