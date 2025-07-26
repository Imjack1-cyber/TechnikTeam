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

		T instance = injector.getInstance(endpointClass);
		injector.injectMembers(instance); 

		return instance;
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		super.modifyHandshake(sec, request, response);
		new GetHttpSessionConfigurator().modifyHandshake(sec, request, response);
	}
}