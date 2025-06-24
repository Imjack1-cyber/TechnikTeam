package de.technikteam.servlet;

import de.technikteam.dao.PasskeyCredentialDAO;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Handles the display and management of a user's own passkeys.
 */
@WebServlet("/passkeys")
public class PasskeyManagementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private PasskeyCredentialDAO passkeyDAO;

	@Override
	public void init() throws ServletException {
		passkeyDAO = new PasskeyCredentialDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		List<PasskeyCredential> passkeys = passkeyDAO.getCredentialsForUser(user.getId());
		request.setAttribute("passkeys", passkeys);
		request.getRequestDispatcher("/passkeys.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		String action = request.getParameter("action");
		if ("delete".equals(action)) {
			try {
				int credentialId = Integer.parseInt(request.getParameter("id"));
				if (passkeyDAO.deleteCredential(credentialId, user.getId())) {
					request.getSession().setAttribute("successMessage", "Passkey erfolgreich entfernt.");
				} else {
					request.getSession().setAttribute("errorMessage", "Passkey konnte nicht entfernt werden.");
				}
			} catch (NumberFormatException e) {
				request.getSession().setAttribute("errorMessage", "Ungültige Passkey-ID.");
			}
		}
		response.sendRedirect(request.getContextPath() + "/passkeys");
	}
}