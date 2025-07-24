package de.technikteam.servlet;

import com.google.inject.Singleton;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@Singleton
public class RootServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User user = (session != null) ? (User) session.getAttribute("user") : null;

		if (user != null) {
			// User is logged in, redirect to the home page.
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			// User is not logged in, redirect to the login page.
			response.sendRedirect(request.getContextPath() + "/login");
		}
	}
}