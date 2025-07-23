package de.technikteam.servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.DashboardDataDTO;
import de.technikteam.service.AdminDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * API endpoint to provide all necessary data for the interactive admin
 * dashboard. This servlet is protected by the AdminFilter.
 */
@WebServlet("/api/admin/dashboard-data")
public class AdminDashboardApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AdminDashboardService dashboardService;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		dashboardService = new AdminDashboardService();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		DashboardDataDTO data = dashboardService.getDashboardData();
		String jsonResponse = gson.toJson(data);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonResponse);
	}
}