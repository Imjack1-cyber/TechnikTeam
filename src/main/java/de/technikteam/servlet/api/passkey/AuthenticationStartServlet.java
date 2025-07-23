package de.technikteam.servlet.api.passkey;

import de.technikteam.service.PasskeyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/auth/passkey/login/start")
public class AuthenticationStartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PasskeyService passkeyService = new PasskeyService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String challengeJson = passkeyService.startAuthentication(username);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(challengeJson);
    }
}