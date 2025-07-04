package de.technikteam.servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Mapped to /admin/resource-calendar, this servlet's sole purpose is to forward
 * the request to the JSP page that will render the resource timeline view. The
 * actual event data is fetched by the calendar's JavaScript from the
 * ResourceCalendarApiServlet.
 */
@WebServlet("/admin/resource-calendar")
public class ResourceCalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/admin/resource_calendar.jsp").forward(request, response);
	}
}