package de.technikteam.servlet.api;

import com.google.gson.Gson;
import de.technikteam.model.SystemStatsDTO;
import de.technikteam.service.SystemInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides system statistics as a JSON API endpoint. This servlet is protected
 * by the AdminFilter.
 */
@WebServlet("/api/admin/system-stats")
public class SystemStatsApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SystemInfoService systemInfoService;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		systemInfoService = new SystemInfoService();
		gson = new Gson();
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