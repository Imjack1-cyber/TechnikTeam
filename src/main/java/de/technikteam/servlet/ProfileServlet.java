package de.technikteam.servlet;

import de.technikteam.dao.AchievementDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.*;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/profil")
public class ProfileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private UserQualificationsDAO qualificationsDAO;
	private UserDAO userDAO;
	private AchievementDAO achievementDAO;
	private PasskeyDAO passkeyDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		qualificationsDAO = new UserQualificationsDAO();
		userDAO = new UserDAO();
		achievementDAO = new AchievementDAO();
		passkeyDAO = new PasskeyDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		List<Event> eventHistory = eventDAO.getEventHistoryForUser(user.getId());
		List<UserQualification> qualifications = qualificationsDAO.getQualificationsForUser(user.getId());
		List<Achievement> achievements = achievementDAO.getAchievementsForUser(user.getId());
		List<PasskeyCredential> passkeys = passkeyDAO.getCredentialsByUserId(user.getId());

		request.setAttribute("eventHistory", eventHistory);
		request.setAttribute("qualifications", qualifications);
		request.setAttribute("achievements", achievements);
		request.setAttribute("passkeys", passkeys);

		request.getRequestDispatcher("/views/public/profile.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = request.getParameter("action");
        if ("deletePasskey".equals(action)) {
            handleDeletePasskey(request, response, user);
            return;
        }

		String chatColor = request.getParameter("chatColor");

		if (user != null && chatColor != null) {
			if (userDAO.updateUserChatColor(user.getId(), chatColor)) {
				user.setChatColor(chatColor);
				session.setAttribute("user", user);
				session.setAttribute("successMessage", "Chat-Farbe erfolgreich gespeichert!");
			} else {
				session.setAttribute("errorMessage", "Farbe konnte nicht gespeichert werden.");
			}
		}
		response.sendRedirect(request.getContextPath() + "/profil");
	}

	private void handleDeletePasskey(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        try {
            int credentialDbId = Integer.parseInt(request.getParameter("credentialId"));
            if (passkeyDAO.deleteCredential(credentialDbId, user.getId())) {
                request.getSession().setAttribute("successMessage", "Passkey erfolgreich entfernt.");
            } else {
                request.getSession().setAttribute("errorMessage", "Passkey konnte nicht entfernt werden.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Ungültige Passkey-ID.");
        }
        response.sendRedirect(request.getContextPath() + "/profil");
    }
}