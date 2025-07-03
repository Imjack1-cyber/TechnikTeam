package de.technikteam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet handles the request for the main calendar page. Its sole purpose
 * is to forward the user to the calendar.jsp page, which then uses JavaScript
 * to fetch data from the CalendarApiServlet.
 */
@WebServlet("/kalender")
public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Forwards the request to the calendar display page.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// CORRECTED: Forward to the actual JSP file path.
		request.getRequestDispatcher("/views/public/calendar.jsp").forward(request, response);
	}
}