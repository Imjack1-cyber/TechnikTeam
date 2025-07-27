package de.technikteam.api.v1.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import de.technikteam.service.PasskeyService;
import de.technikteam.util.CSRFUtil;
import de.technikteam.util.NavigationRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AuthenticationFinishServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PasskeyService passkeyService;
	private final Gson gson;

	@Inject
	public AuthenticationFinishServlet(PasskeyService passkeyService) {
		this.passkeyService = passkeyService;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String credentialJson = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

		User user = passkeyService.finishAuthentication(credentialJson);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		if (user != null) {
			HttpSession session = request.getSession(true);
			session.setAttribute("user", user);
			CSRFUtil.storeToken(session);
			List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(user);
			session.setAttribute("navigationItems", navigationItems);

			response.getWriter().write(gson.toJson(new ApiResponse(true, "Login successful!", user)));
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write(gson.toJson(new ApiResponse(false, "Passkey authentication failed.", null)));
		}
	}
}