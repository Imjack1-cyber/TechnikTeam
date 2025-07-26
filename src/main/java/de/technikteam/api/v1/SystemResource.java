// src/main/java/de/technikteam/api/v1/SystemResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.SystemStatsDTO;
import de.technikteam.model.User;
import de.technikteam.service.SystemInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class SystemResource extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final SystemInfoService systemInfoService;
	private final Gson gson;

	@Inject
	public SystemResource(SystemInfoService systemInfoService, Gson gson) {
		this.systemInfoService = systemInfoService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("SYSTEM_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		SystemStatsDTO stats = systemInfoService.getSystemStats();
		sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "System stats retrieved", stats));
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