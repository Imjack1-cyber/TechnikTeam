package de.technikteam.servlet.api.passkey;

import com.google.gson.Gson;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.PasskeyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/api/auth/passkey/register/finish")
public class RegistrationFinishServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PasskeyService passkeyService = new PasskeyService();
	private final Gson gson = new Gson();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String deviceName = request.getParameter("deviceName");
		String credentialJson = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

		boolean success = passkeyService.finishRegistration(user.getId(), credentialJson, deviceName);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		if (success) {
			response.getWriter().write(gson.toJson(ApiResponse.success("Device registered successfully!")));
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(gson.toJson(ApiResponse.error("Failed to register device.")));
		}
	}
}