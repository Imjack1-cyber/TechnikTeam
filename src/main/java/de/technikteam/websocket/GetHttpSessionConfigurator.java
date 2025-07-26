package de.technikteam.websocket;

import de.technikteam.model.User;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {

	public static final String USER_PROPERTY_KEY = "user";
	private static volatile ServletContext servletContext;

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		if (httpSession != null) {
			if (servletContext == null) {
				servletContext = httpSession.getServletContext();
			}

			User user = (User) httpSession.getAttribute(USER_PROPERTY_KEY);
			if (user != null) {
				sec.getUserProperties().put(USER_PROPERTY_KEY, user);
			}
		}
	}

	public static ServletContext getServletContext() {
		return servletContext;
	}
}