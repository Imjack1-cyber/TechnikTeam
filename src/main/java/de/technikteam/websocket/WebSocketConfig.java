package de.technikteam.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final EventChatSocketHandler eventChatSocketHandler;
	private final ChatWebSocketHandler chatWebSocketHandler;

	@Autowired
	public WebSocketConfig(EventChatSocketHandler eventChatSocketHandler, ChatWebSocketHandler chatWebSocketHandler) {
		this.eventChatSocketHandler = eventChatSocketHandler;
		this.chatWebSocketHandler = chatWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(eventChatSocketHandler, "/ws/chat/{eventId}").setAllowedOrigins("*");
		registry.addHandler(chatWebSocketHandler, "/ws/dm/{conversationId}").setAllowedOrigins("*");
	}
}