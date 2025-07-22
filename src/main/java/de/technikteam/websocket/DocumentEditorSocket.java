package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.technikteam.config.Permissions;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.util.MarkdownUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * WebSocket endpoint for real-time collaborative document editing. This version
 * uses a full-text synchronization model.
 */
@ServerEndpoint(value = "/ws/editor/{fileId}", configurator = GetHttpSessionConfigurator.class)
public class DocumentEditorSocket {

	private static final Logger logger = LogManager.getLogger(DocumentEditorSocket.class);
	private static final FileDAO fileDAO = new FileDAO();
	private static final Gson gson = new Gson();

	@OnOpen
	public void onOpen(Session session, @PathParam("fileId") String fileId, EndpointConfig config) throws IOException {
		User user = (User) config.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);

		if (user == null || (!user.getPermissions().contains(Permissions.FILE_UPDATE)
				&& !user.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL))) {
			logger.warn("Unauthorized WebSocket connection attempt for editor on file ID {}. User: {}", fileId,
					user != null ? user.getUsername() : "GUEST");
			session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Permission denied."));
			return;
		}

		session.getUserProperties().put(GetHttpSessionConfigurator.USER_PROPERTY_KEY, user);
		DocumentSessionManager.getInstance().addSession(fileId, session);
	}

	@OnMessage
	public void onMessage(Session session, String message, @PathParam("fileId") String fileId) {
		User user = (User) session.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);
		if (user == null)
			return;

		try {
			Map<String, String> messageMap = gson.fromJson(message, Map.class);
			String type = messageMap.get("type");
			String content = messageMap.get("payload");

			if ("content_update".equals(type)) {
				handleContentUpdate(session, fileId, content);
			}
		} catch (JsonSyntaxException e) {
			logger.error("Invalid JSON received from user '{}' for file ID {}: {}", user.getUsername(), fileId, message,
					e);
		}
	}

	private void handleContentUpdate(Session originSession, String fileIdStr, String content) {
		try {
			int fileId = Integer.parseInt(fileIdStr);
			de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);

			if (dbFile != null) {
				// Sanitize the content on the server-side to prevent stored XSS
				String sanitizedContent = MarkdownUtil.sanitize(content);

				// Persist the changes to the disk
				if (fileDAO.updateFileContent(dbFile.getFilepath(), sanitizedContent)) {
					fileDAO.touchFileRecord(fileId); // Update timestamp

					// Broadcast the full sanitized content to other connected clients
					Map<String, String> broadcastPayload = Map.of("type", "content_update", "payload",
							sanitizedContent);
					DocumentSessionManager.getInstance().broadcastExcept(fileIdStr, gson.toJson(broadcastPayload),
							originSession);
				} else {
					logger.error("Failed to save file content to disk for file ID: {}", fileIdStr);
				}
			} else {
				logger.warn("Received content update for non-existent file ID: {}", fileIdStr);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid fileId '{}' received in WebSocket message.", fileIdStr);
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam("fileId") String fileId) {
		User user = (User) session.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);
		String username = (user != null) ? user.getUsername() : "[unauthenticated]";
		logger.info("Editor WebSocket session for user '{}' on file {} closed.", username, fileId);
		DocumentSessionManager.getInstance().removeSession(fileId, session);
	}

	@OnError
	public void onError(Session session, Throwable throwable, @PathParam("fileId") String fileId) {
		logger.error("WebSocket ERROR in editor for file [{}], session [{}]:", fileId, session.getId(), throwable);
	}
}