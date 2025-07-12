package de.technikteam.websocket;

import de.technikteam.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * A custom WebSocket configurator that intercepts the WebSocket handshake
 * process. Its primary purpose is to retrieve the authenticated User object
 * from the HttpSession and make it available to the WebSocket endpoint's
 * session properties. This is a standard pattern for securing WebSocket
 * connections.
 */
public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {

	public static final String USER_PROPERTY_KEY = "user";

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		if (httpSession != null) {
			User user = (User) httpSession.getAttribute(USER_PROPERTY_KEY);
			if (user != null) {
				// Put the authenticated User object into the WebSocket session's user
				// properties.
				sec.getUserProperties().put(USER_PROPERTY_KEY, user);
			}
		}
	}
}