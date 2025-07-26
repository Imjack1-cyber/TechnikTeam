// src/main/java/de/technikteam/servlet/ProfileServlet.java
package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class ProfileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Inject
	public ProfileServlet() {
		// All DAO dependencies are removed as this is now just a shell dispatcher.
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		// The servlet now only forwards to the JSP. All data is loaded by profile.js.
		request.getRequestDispatcher("/views/public/profile.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// All POST logic has been migrated to the PublicProfileResource.
		// Redirecting to GET is a safe fallback.
		response.sendRedirect(request.getContextPath() + "/profil");
	}
}