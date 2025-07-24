package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Singleton
public class AdminChangeRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final ProfileChangeRequestDAO requestDAO;
	private final UserDAO userDAO;

	@Inject
	public AdminChangeRequestServlet(ProfileChangeRequestDAO requestDAO, UserDAO userDAO) {
		this.requestDAO = requestDAO;
		this.userDAO = userDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (!user.getPermissions().contains("USER_UPDATE") && !user.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		List<ProfileChangeRequest> pendingRequests = requestDAO.getPendingRequests();
		request.setAttribute("pendingRequests", pendingRequests);
		request.getRequestDispatcher("/views/admin/admin_requests.jsp").forward(request, response);
	}
}