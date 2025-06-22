package de.technikteam.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * A singleton service designed to handle real-time notifications using
 * Server-Sent Events (SSE). It manages a thread-safe list of connected clients
 * (via their `AsyncContext`) and provides a central method to broadcast a
 * notification message to all of them simultaneously.
 */
public class NotificationService {
	private static final Logger logger = LogManager.getLogger(NotificationService.class);
	private static final NotificationService INSTANCE = new NotificationService();

	// Use a thread-safe list because clients can register/deregister from different
	// threads.
	private final List<AsyncContext> contexts = new CopyOnWriteArrayList<>();

	private NotificationService() {
	}

	public static NotificationService getInstance() {
		return INSTANCE;
	}

	/**
	 * Registers a new client (from an incoming HttpServletRequest) to receive
	 * notifications. It starts an asynchronous context and adds it to the list of
	 * subscribers.
	 * 
	 * @param request The request from the client wishing to subscribe.
	 */
	public void register(HttpServletRequest request) {
		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0); // No timeout for SSE connections
		contexts.add(asyncContext);
		logger.info("New client registered for SSE notifications. Total clients: {}", contexts.size());
	}

	/**
	 * Sends a notification message to all currently registered clients. If a client
	 * has disconnected, it will be removed from the list.
	 * 
	 * @param message The message to send.
	 */
	public void sendNotification(String message) {
		logger.info("Sending notification to {} clients: {}", contexts.size(), message);
		for (AsyncContext context : contexts) {
			try {
				PrintWriter writer = context.getResponse().getWriter();
				// Format the message according to the SSE specification: "data: message\n\n"
				writer.write("data: " + message + "\n\n");
				writer.flush();
			} catch (IOException e) {
				logger.warn("Failed to send notification to a client (likely disconnected), removing it.", e);
				contexts.remove(context);
			}
		}
	}
}