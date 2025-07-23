package de.technikteam.servlet.admin;

import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/requests")
public class AdminChangeRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ProfileChangeRequestDAO requestDAO;
	private UserDAO userDAO;

	@Override
	public void init() {
		requestDAO = new ProfileChangeRequestDAO();
		userDAO = new UserDAO();
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

		// Fetch original user data to show a diff
		for (ProfileChangeRequest req : pendingRequests) {
			User originalUser = userDAO.getUserById(req.getUserId());
			req.getRequestedChanges(); // This is just a placeholder for more complex logic in JSP
		}

		request.setAttribute("pendingRequests", pendingRequests);
		request.getRequestDispatcher("/views/admin/admin_requests.jsp").forward(request, response);
	}
}