package de.technikteam.websocket;

import com.google.inject.Injector;
import jakarta.servlet.ServletContext;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class GuiceAwareServerEndpointConfigurator extends ServerEndpointConfig.Configurator {

	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		ServletContext servletContext = GetHttpSessionConfigurator.getServletContext();
		if (servletContext == null) {
			throw new IllegalStateException("ServletContext is not available. Cannot find Guice Injector.");
		}

		Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());
		if (injector == null) {
			throw new IllegalStateException(
					"Guice Injector not found in ServletContext. Is GuiceConfig configured correctly in web.xml?");
		}

		// Guice creates the instance and injects its constructor dependencies (if any).
		// For WebSockets, we often use a static injection pattern, so we get the
		// instance
		// first and then ask Guice to inject its members (including static ones if
		// configured).
		T instance = injector.getInstance(endpointClass);
		injector.injectMembers(instance); // This is the key part for member injection.

		return instance;
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		// Also perform the HttpSession capture
		super.modifyHandshake(sec, request, response);
		new GetHttpSessionConfigurator().modifyHandshake(sec, request, response);
	}
}