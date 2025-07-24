package de.technikteam.servlet.api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.SystemStatsDTO;
import de.technikteam.service.SystemInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class SystemStatsApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final SystemInfoService systemInfoService;
	private final Gson gson;

	@Inject
	public SystemStatsApiServlet(SystemInfoService systemInfoService) {
		this.systemInfoService = systemInfoService;
		this.gson = new Gson();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		SystemStatsDTO stats = systemInfoService.getSystemStats();
		String jsonResponse = gson.toJson(stats);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonResponse);
	}
}