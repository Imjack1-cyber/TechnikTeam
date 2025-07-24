package de.technikteam.servlet;

import java.io.IOException;

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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			logger.warn("Unauthorized attempt to connect to SSE stream. No session.");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		de.technikteam.model.User user = (de.technikteam.model.User) session.getAttribute("user");
		logger.info("Client '{}' connecting to SSE stream.", user.getUsername());

		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");

		NotificationService.getInstance().register(request);
	}
}