package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
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

@ServerEndpoint(value = "/ws/editor/{fileId}", configurator = GuiceAwareServerEndpointConfigurator.class)
public class DocumentEditorSocket {

	private static final Logger logger = LogManager.getLogger(DocumentEditorSocket.class);
	private static FileDAO fileDAO;
	private static final Gson gson = new Gson();

	@Inject
	public static void setDependencies(FileDAO fileDAO) {
		DocumentEditorSocket.fileDAO = fileDAO;
	}

	@OnOpen
	public void onOpen(Session session, @PathParam("fileId") String fileId, EndpointConfig config) throws IOException {
		User user = (User) config.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);

		if (user == null || (!user.getPermissions().contains(Permissions.FILE_UPDATE)
				&& !user.getPermissions().contains("ACCESS_ADMIN_PANEL"))) {
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
			@SuppressWarnings("unchecked")
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
				String sanitizedContent = MarkdownUtil.sanitize(content);
				if (fileDAO.updateFileContent(dbFile.getFilepath(), sanitizedContent)) {
					fileDAO.touchFileRecord(fileId);
					Map<String, String> broadcastPayload = Map.of("type", "content_update", "payload",
							sanitizedContent);
					DocumentSessionManager.getInstance().broadcastExcept(fileIdStr, gson.toJson(broadcastPayload),
							originSession);
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid fileId '{}' received in WebSocket message.", fileIdStr);
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam("fileId") String fileId) {
		DocumentSessionManager.getInstance().removeSession(fileId, session);
	}

	@OnError
	public void onError(Session session, Throwable throwable, @PathParam("fileId") String fileId) {
		logger.error("WebSocket ERROR in editor for file [{}], session [{}]:", fileId, session.getId(), throwable);
	}
}