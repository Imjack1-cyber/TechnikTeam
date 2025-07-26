// src/main/java/de/technikteam/servlet/EventServlet.java
package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class EventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Inject
	public EventServlet() {
		// Dependencies no longer needed
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// This servlet now only serves the shell page.
		request.getRequestDispatcher("/views/public/events.jsp").forward(request, response);
	}
}