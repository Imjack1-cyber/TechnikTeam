package de.technikteam.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public class IdentityVerificationRequest {
    private long id;
    private int userId;
    private String challengeToken;
    private String requestType; // 'PASSWORD_RESET', 'MFA_LOGIN'
    private String status; // 'PENDING', 'APPROVED', 'DENIED', 'EXPIRED', 'COMPLETED'
    private String context; // JSON for extra data
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // Transient
    private String username;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public void setChallengeToken(String challengeToken) {
        this.challengeToken = challengeToken;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContext() {
        return context;
    }

    public Map<String, Object> getContextAsMap() {
        if (this.context == null || this.context.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return new Gson().fromJson(this.context, type);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public void setContext(String context) {
        this.context = context;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}