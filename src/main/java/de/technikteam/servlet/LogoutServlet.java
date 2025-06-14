package de.technikteam.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Servlet to handle user logout.
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String username = "Gast";

		if (session != null) {
			// Retrieve username before invalidating session
			if (session.getAttribute("username") != null) {
				username = (String) session.getAttribute("username");
			}
			// Invalidate the session to log the user out
			session.invalidate();
		}

		// Redirect to the logout page with the username as a parameter
		response.sendRedirect("logout.jsp?username=" + java.net.URLEncoder.encode(username, "UTF-8"));
	}
}