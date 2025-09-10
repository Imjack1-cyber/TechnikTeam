package de.technikteam.websocket;

import com.google.gson.Gson;
import de.technikteam.model.File;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.FileService;
import de.technikteam.dao.FileDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.Map;

@Component
public class FileEditorSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(FileEditorSocketHandler.class);

    private final FileDAO fileDAO;
    private final FileService fileService;
    private final ChatSessionManager sessionManager;
    private final Gson gson = new Gson();
    private final PolicyFactory richTextPolicy;

    @Autowired
    public FileEditorSocketHandler(FileDAO fileDAO, FileService fileService, ChatSessionManager sessionManager,
                                   @Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
        this.fileDAO = fileDAO;
        this.fileService = fileService;
        this.sessionManager = sessionManager;
        this.richTextPolicy = richTextPolicy;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = getUserFromSession(session);
        String fileIdStr = getFileId(session);

        if (user == null || fileIdStr == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing user or file ID."));
            return;
        }

        int fileId = Integer.parseInt(fileIdStr);
        File file = fileDAO.getFileById(fileId);

        // Authorization check: Only admins or users for public .md files can connect
        if (file == null || (!"NUTZER".equals(file.getRequiredRole()) && !user.hasAdminAccess())) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Unauthorized to edit this file."));
            return;
        }

        session.getAttributes().put("user", user);
        session.getAttributes().put("fileId", fileIdStr);
        sessionManager.addSession(fileIdStr, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        String fileIdStr = (String) session.getAttributes().get("fileId");
        if (user == null || fileIdStr == null) return;

        int fileId = Integer.parseInt(fileIdStr);

        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = gson.fromJson(message.getPayload(), Map.class);
        String type = (String) payloadMap.get("type");

        if ("content_update".equals(type)) {
            @SuppressWarnings("unchecked")
            Map<String, String> payload = (Map<String, String>) payloadMap.get("payload");
            String newContent = payload.get("content");
            String sanitizedContent = richTextPolicy.sanitize(newContent);

            // Save to disk (and log action)
            fileService.updateFileContent(fileId, sanitizedContent, user);

            // Broadcast the sanitized content to all clients in the session
            Map<String, Object> broadcastPayload = Map.of("type", "content_update", "payload", Map.of("content", sanitizedContent));
            sessionManager.broadcast(fileIdStr, gson.toJson(broadcastPayload));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String fileId = (String) session.getAttributes().get("fileId");
        if (fileId != null) {
            sessionManager.removeSession(fileId, session);
        }
    }

    private User getUserFromSession(WebSocketSession session) {
        if (session.getPrincipal() instanceof Authentication auth) {
            if (auth.getPrincipal() instanceof SecurityUser securityUser) {
                return securityUser.getUser();
            }
        }
        return null;
    }

    private String getFileId(WebSocketSession session) {
        if (session.getUri() == null) return null;
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }
}