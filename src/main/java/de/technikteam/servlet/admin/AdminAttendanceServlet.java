// CREATE THIS NEW FILE: src/main/java/de/technikteam/servlet/admin/AdminAttendanceServlet.java
package de.technikteam.servlet.admin;

import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles all actions related to updating attendance records,
 * primarily from the qualification matrix modal.
 */
@WebServlet("/admin/attendance")
public class AdminAttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MeetingAttendanceDAO attendanceDAO;

    @Override
    public void init() {
        attendanceDAO = new MeetingAttendanceDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        User adminUser = (User) request.getSession().getAttribute("user");
        String returnTo = request.getParameter("returnTo");

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            int meetingId = Integer.parseInt(request.getParameter("meetingId"));
            
            // A checkbox sends "true" if checked, and null if not. This is how we check it.
            boolean attended = "true".equals(request.getParameter("attended"));
            
            String remarks = request.getParameter("remarks");

            if (attendanceDAO.setAttendance(userId, meetingId, attended, remarks)) {
                // Log the action
                String status = attended ? "TEILGENOMMEN" : "NICHT TEILGENOMMEN";
                String logDetails = String.format("Teilnahme für Nutzer-ID %d bei Meeting-ID %d auf '%s' gesetzt.", userId, meetingId, status);
                AdminLogService.log(adminUser.getUsername(), "UPDATE_ATTENDANCE", logDetails);
                
                request.getSession().setAttribute("successMessage", "Teilnahmestatus erfolgreich aktualisiert.");
            } else {
                request.getSession().setAttribute("errorMessage", "Fehler: Teilnahmestatus konnte nicht aktualisiert werden.");
            }

        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Fehler: Ungültige ID empfangen.");
        }

        // Redirect back to the matrix or a default page
        if ("matrix".equals(returnTo)) {
            response.sendRedirect(request.getContextPath() + "/admin/matrix");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }
}