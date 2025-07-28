package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Singleton
public class PublicFilesResource extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final FileDAO fileDAO;
	private final Gson gson;

	@Inject
	public PublicFilesResource(FileDAO fileDAO, Gson gson) {
		this.fileDAO = fileDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		Map<String, List<de.technikteam.model.File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(user);
		sendJsonResponse(resp, HttpServletResponse.SC_OK,
				new ApiResponse(true, "Files retrieved successfully.", groupedFiles));
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