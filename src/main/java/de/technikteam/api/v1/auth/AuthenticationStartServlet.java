package de.technikteam.api.v1.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.service.PasskeyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class AuthenticationStartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PasskeyService passkeyService;

	@Inject
	public AuthenticationStartServlet(PasskeyService passkeyService) {
		this.passkeyService = passkeyService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String challengeJson = passkeyService.startAuthentication(username);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(challengeJson);
	}
}