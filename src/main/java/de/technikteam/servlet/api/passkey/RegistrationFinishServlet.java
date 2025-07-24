package de.technikteam.servlet.api.passkey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.PasskeyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Singleton
public class RegistrationFinishServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PasskeyService passkeyService;
	private final Gson gson;

	@Inject
	public RegistrationFinishServlet(PasskeyService passkeyService) {
		this.passkeyService = passkeyService;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

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