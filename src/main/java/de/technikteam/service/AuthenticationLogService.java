package de.technikteam.service;

import de.technikteam.dao.AuthenticationLogDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.AuthenticationLog;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthenticationLogService {

    private final AuthenticationLogDAO authLogDAO;
    private final UserDAO userDAO;

    @Autowired
    public AuthenticationLogService(AuthenticationLogDAO authLogDAO, UserDAO userDAO) {
        this.authLogDAO = authLogDAO;
        this.userDAO = userDAO;
    }

    public void logLoginSuccess(int userId, String username, String ipAddress, String jti, Instant tokenExpiry) {
        AuthenticationLog log = new AuthenticationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setEventType("LOGIN_SUCCESS");
        log.setJti(jti);
        if (tokenExpiry != null) {
            log.setTokenExpiry(tokenExpiry.atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        authLogDAO.createLog(log);
    }

    public void logLoginFailure(String username, String ipAddress) {
        User user = userDAO.getUserByUsername(username); // Can be null
        AuthenticationLog log = new AuthenticationLog();
        log.setUserId(user != null ? user.getId() : null);
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setEventType("LOGIN_FAILURE");
        authLogDAO.createLog(log);
    }

    public void logLogout(int userId, String username, String ipAddress) {
        AuthenticationLog log = new AuthenticationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setEventType("LOGOUT");
        authLogDAO.createLog(log);
    }

    public AuthenticationLog getPreviousLoginInfo(int userId) {
        return authLogDAO.getPreviousLoginInfo(userId);
    }

    public LocalDateTime getTimestampOfLastLogin(int userId) {
        return authLogDAO.getTimestampOfLastLogin(userId);
    }
}