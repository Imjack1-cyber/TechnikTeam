package de.technikteam.servlet.admin.action;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.stream.Collectors;

public class ResetPasswordAction implements Action {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User adminUser = (User) session.getAttribute("user");

        if (!adminUser.getPermissions().contains("USER_PASSWORD_RESET") && !adminUser.hasAdminAccess()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return null;
        }

        int userId = Integer.parseInt(request.getParameter("userId"));
        User userToReset = userDAO.getUserById(userId);

        if (userToReset == null) {
            session.setAttribute("errorMessage", "Benutzer zum Zurücksetzen nicht gefunden.");
        } else {
            String newPassword = generateRandomPassword(12);
            if (userDAO.changePassword(userId, newPassword)) {
                String logDetails = String.format("Passwort für Benutzer '%s' (ID: %d) zurückgesetzt.",
                        userToReset.getUsername(), userId);
                AdminLogService.log(adminUser.getUsername(), "RESET_PASSWORD", logDetails);

                session.setAttribute("passwordResetUser", userToReset.getUsername());
                session.setAttribute("passwordResetNewPassword", newPassword);
            } else {
                session.setAttribute("errorMessage", "Passwort konnte nicht zurückgesetzt werden.");
            }
        }

        return "redirect:/admin/mitglieder";
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, chars.length()).mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }
}