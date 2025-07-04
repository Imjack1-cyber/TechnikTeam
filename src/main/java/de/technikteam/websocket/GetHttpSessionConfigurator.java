package de.technikteam.websocket;

import de.technikteam.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {
	
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
	    HttpSession httpSession = (HttpSession) request.getHttpSession();
	    if (httpSession != null) {
	        User user = (User) httpSession.getAttribute("user");
	        if (user != null) {
	            sec.getUserProperties().put("user", user);
	        }
	    }
	}
}