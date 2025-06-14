package de.technikteam.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;

// Singleton service to manage SSE connections
public class NotificationService {
	private static final Logger logger = LogManager.getLogger(NotificationService.class);
	private static final NotificationService INSTANCE = new NotificationService();
	private final List<AsyncContext> contexts = new CopyOnWriteArrayList<>();

	private NotificationService() {
	}

	public static NotificationService getInstance() {
		return INSTANCE;
	}

	public void register(HttpServletRequest request) {
		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0); // No timeout for this connection
		contexts.add(asyncContext);
		logger.info("New client registered for SSE notifications. Total clients: {}", contexts.size());
	}

	public void sendNotification(String message) {
		logger.info("Sending notification to all clients: {}", message);
		for (AsyncContext context : contexts) {
			try {
				PrintWriter writer = context.getResponse().getWriter();
				writer.write("data: " + message + "\n\n"); // SSE format: "data: message\n\n"
				writer.flush();
			} catch (IOException e) {
				logger.warn("Failed to send notification to a client, removing it.", e);
				contexts.remove(context); // Client has disconnected
			}
		}
	}
}