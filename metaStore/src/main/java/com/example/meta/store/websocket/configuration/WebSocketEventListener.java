package com.example.meta.store.websocket.configuration;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

	
	private final SimpMessageSendingOperations messageTemplate;
	
	
	@EventListener
	public void handelDisconnectingListener(SessionDisconnectEvent event ) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String username = (String) headerAccessor.getSessionAttributes().get("username");
		if(username != null) {
			log.info("user name is " +username);
			
			messageTemplate.convertAndSend("/topic/public",username);
		}
	}
	
}
