package com.example.meta.store.websocket.configuration;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.werehouse.Entities.Message;

@RestController
public class ChatController {

	@SendTo("/topic/public")
	public Message SendMessage(@Payload Message message) {
		return  message;
	}
	
	@SendTo("/topic/pubic")
	public Message addUser(@Payload Message message , SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", message.getConversation().getUser1().getUsername());
		return message;
	}

}
