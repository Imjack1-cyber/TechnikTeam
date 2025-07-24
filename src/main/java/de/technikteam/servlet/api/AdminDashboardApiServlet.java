package de.technikteam.servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.DashboardDataDTO;
import de.technikteam.service.AdminDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Singleton
public class AdminDashboardApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final AdminDashboardService dashboardService;
	private final Gson gson;

	@Inject
	public AdminDashboardApiServlet(AdminDashboardService dashboardService) {
		this.dashboardService = dashboardService;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
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