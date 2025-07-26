package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.*;
import de.technikteam.model.*;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Singleton
public class ProfileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ProfileServlet.class);
	private final EventDAO eventDAO;
	private final UserQualificationsDAO qualificationsDAO;
	private final UserDAO userDAO;
	private final AchievementDAO achievementDAO;
	private final PasskeyDAO passkeyDAO;
	private final ProfileChangeRequestDAO requestDAO;
	private final Gson gson = new Gson();

	@Inject
	public ProfileServlet(EventDAO eventDAO, UserQualificationsDAO qualificationsDAO, UserDAO userDAO,
			AchievementDAO achievementDAO, PasskeyDAO passkeyDAO, ProfileChangeRequestDAO requestDAO) {
		this.eventDAO = eventDAO;
		this.qualificationsDAO = qualificationsDAO;
		this.userDAO = userDAO;
		this.achievementDAO = achievementDAO;
		this.passkeyDAO = passkeyDAO;
		this.requestDAO = requestDAO;
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
		boolean hasPendingRequest = requestDAO.hasPendingRequest(user.getId());

		request.setAttribute("eventHistory", eventHistory);
		request.setAttribute("qualifications", qualifications);
		request.setAttribute("achievements", achievements);
		request.setAttribute("passkeys", passkeys);
		request.setAttribute("hasPendingRequest", hasPendingRequest);

		request.getRequestDispatcher("/views/public/profile.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		String action = request.getParameter("action");

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for profile action '{}' by user '{}'", action,
					user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		switch (action != null ? action : "") {
		case "deletePasskey":
			handleDeletePasskey(request, response, user);
			break;
		case "updateChatColor":
			handleUpdateChatColor(request, response, user);
			break;
		case "requestProfileChange":
			handleProfileChangeRequest(request, response, user);
			break;
		default:
			logger.warn("Unknown POST action '{}' received for /profil", action);
			response.sendRedirect(request.getContextPath() + "/profil");
			break;
		}
	}

	private void handleProfileChangeRequest(HttpServletRequest request, HttpServletResponse response, User currentUser)
			throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Map<String, String> changes = new HashMap<>();
		String newEmail = request.getParameter("email");
		if (!Objects.equals(currentUser.getEmail(), newEmail)) {
			changes.put("email", newEmail);
		}

		int newClassYear = 0;
		try {
			String classYearParam = request.getParameter("classYear");
			if (classYearParam != null && !classYearParam.isEmpty()) {
				newClassYear = Integer.parseInt(classYearParam);
			}
		} catch (NumberFormatException e) {
			/* keep 0 */ }
		if (currentUser.getClassYear() != newClassYear) {
			changes.put("classYear", String.valueOf(newClassYear));
		}

		String newClassName = request.getParameter("className");
		if (!Objects.equals(currentUser.getClassName(), newClassName)) {
			changes.put("className", newClassName);
		}

		if (changes.isEmpty()) {
			response.getWriter().write(gson.toJson(new ApiResponse(false, "Keine Änderungen festgestellt.", null)));
			return;
		}

		ProfileChangeRequest pcr = new ProfileChangeRequest();
		pcr.setUserId(currentUser.getId());
		pcr.setRequestedChanges(gson.toJson(changes));

		if (requestDAO.createRequest(pcr)) {
			response.getWriter()
					.write(gson.toJson(new ApiResponse(true, "Änderungsantrag erfolgreich eingereicht.", null)));
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter()
					.write(gson.toJson(new ApiResponse(false, "Antrag konnte nicht gespeichert werden.", null)));
		}
	}

	private void handleUpdateChatColor(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException {
		String chatColor = request.getParameter("chatColor");
		if (user != null && chatColor != null) {
			if (userDAO.updateUserChatColor(user.getId(), chatColor)) {
				user.setChatColor(chatColor);
				request.getSession().setAttribute("user", user);
				request.getSession().setAttribute("successMessage", "Chat-Farbe erfolgreich gespeichert!");
			} else {
				request.getSession().setAttribute("errorMessage", "Farbe konnte nicht gespeichert werden.");
			}
		}
		response.sendRedirect(request.getContextPath() + "/profil");
	}

	private void handleDeletePasskey(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException {
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