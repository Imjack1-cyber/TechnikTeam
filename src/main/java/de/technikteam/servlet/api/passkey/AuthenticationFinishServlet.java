package de.technikteam.servlet.api.passkey;

import com.google.gson.Gson;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import de.technikteam.service.PasskeyService;
import de.technikteam.util.CSRFUtil;
import de.technikteam.util.NavigationRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/auth/passkey/login/finish")
public class AuthenticationFinishServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PasskeyService passkeyService = new PasskeyService();
	private final Gson gson = new Gson();

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

			response.getWriter().write(gson.toJson(ApiResponse.success("Login successful!", user)));
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write(gson.toJson(ApiResponse.error("Passkey authentication failed.")));
		}
	}
}