package de.technikteam.servlet.api.passkey;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import de.technikteam.service.PasskeyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class RegistrationStartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PasskeyService passkeyService;

	@Inject
	public RegistrationStartServlet(PasskeyService passkeyService) {
		this.passkeyService = passkeyService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String challengeJson = passkeyService.startRegistration(user);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(challengeJson);
	}
}