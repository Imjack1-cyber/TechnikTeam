// src/main/java/de/technikteam/servlet/NotificationServlet.java
package de.technikteam.servlet;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class NotificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(NotificationServlet.class);
	private final NotificationService notificationService;

	@Inject
	public NotificationServlet(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// The legacy AuthenticationFilter places the user object in the request attribute
		// after validating the session.
		de.technikteam.model.User user = (de.technikteam.model.User) request.getAttribute("user");
		
		if (user == null) {
			logger.warn("Unauthorized attempt to connect to SSE stream. No authenticated user found by filter.");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		logger.info("Client '{}' connecting to SSE stream.", user.getUsername());

		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");

		// This call relies on the user object being in the session, which the
		// AuthenticationFilter has already verified exists. The NotificationService will
		// get the session from the request's AsyncContext.
		notificationService.register(request);
	}
}