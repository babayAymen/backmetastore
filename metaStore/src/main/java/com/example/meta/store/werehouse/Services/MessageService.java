package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Mappers.UserMapper;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.ArticleController;
import com.example.meta.store.werehouse.Dtos.ConversationDto;
import com.example.meta.store.werehouse.Dtos.MessageDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Conversation;
import com.example.meta.store.werehouse.Entities.Message;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.MessageType;
import com.example.meta.store.werehouse.Mappers.CompanyMapper;
import com.example.meta.store.werehouse.Mappers.ConversationMapper;
import com.example.meta.store.werehouse.Mappers.MessageMapper;
import com.example.meta.store.werehouse.Repositories.ConversationRepository;
import com.example.meta.store.werehouse.Repositories.MessageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class MessageService extends BaseService<Message, Long> {
	
	private final MessageRepository messageRepository;
	
	private final ConversationRepository conversationRepository;
	
	private final MessageMapper messageMapper;
	
	private final ConversationMapper conversationMapper;
	
	private final UserMapper userMapper;
	
	private final CompanyMapper companyMapper;
	
	private final SimpMessagingTemplate messagingTemplate;

	private final Logger logger = LoggerFactory.getLogger(MessageService.class);
	
	public void sendWSMessage(String userId , Message message) {
		logger.warn("sending message to user {} with payload {}",userId,message);
		messagingTemplate.convertAndSendToUser(userId, "/notifications", message);
	}
	
	public void sendMessage(String message, User receiver, User me, MessageType type) {
		Optional<Conversation> response = conversationRepository.findAllByUser1AndUser2(receiver,me);
		saveNewMessage(response, message, receiver, me, null, null,type);
	}
	
	public void sendMessageCompanyCompany(String message, Company receiver, Company me, MessageType type) {
		Optional<Conversation> response = conversationRepository.findAllByCompany1AndCompany2(receiver,me);
		saveNewMessage(response, message, null, null, receiver, me, type);
	}
	
	public void sendMessageCompanyUser(String message, Company company, User user, Boolean meCompany, MessageType type) {
		Optional<Conversation> response = conversationRepository.findAllByCompany1AndUser2(company,user);
		if(meCompany) {			
		saveNewMessage(response, message, user, null, null, company, type);
		}else {
			saveNewMessage(response, message, null, user, company, null, type);
		}
	}
	private void saveNewMessage(Optional<Conversation> response,String message, User user, User me, Company company, Company myCompany , MessageType type ) {
		Message mess = new Message();
		mess.setContent(message);
//		sendWSMessage(user.getUsername(), mess);
		if(response.isEmpty()) {	
			Conversation conversation  = new Conversation();
			conversation.setUser1(me);
			conversation.setUser2(user);
			conversation.setCompany1(myCompany);
			conversation.setCompany2(company);
			conversation.setType(type);
			conversationRepository.save(conversation);
			mess.setConversation(conversation);
		}else {
			logger.warn("there is a conversation already");
			mess.setConversation(response.get());
		}
		logger.warn("mess saved");
		messageRepository.save(mess);
	}
	

	public List<MessageDto> getAllMyMessage(Long me, Long conversationId, AccountType type) {
		List<Message> messages = new ArrayList<>();
		if(type == AccountType.USER) {			
			messages = messageRepository.findAllByMeAsUserAndConversationId(me,conversationId);
		}else {
			messages = messageRepository.findAllByMeAsCompanyAndConversationId(me,conversationId);	
		}
		List<MessageDto> messagesDto = new ArrayList<>();
		if(!messages.isEmpty()) {
		for(Message i : messages) {
			logger.warn(i.getId()+"id from for loop origin");
			MessageDto dto = messageMapper.mapToDto(i);
			logger.warn(dto.getId()+"id from for loop dto");
			messagesDto.add(dto);
		}
		logger.warn(messagesDto.get(0).getId()+"id size message");
		}
		return messagesDto;
	}
								 
	public List<ConversationDto> getAllMyConversationa(User me){
		List<Conversation> conversations = conversationRepository.findAllByUser1OrUser2(me);
		List<ConversationDto> conversationsDto = new ArrayList<>();
		for(Conversation i : conversations) {
			ConversationDto conver = conversationMapper.mapToDto(i);
			conversationsDto.add(conver);
		}
		return conversationsDto;
	}

	public List<ConversationDto> getAllMyConversation(Long me, int conversationPag, int conversationSize, int messagePag, int messageSize, AccountType type){
		Page<Conversation> conversationPage = null;
		if(type == AccountType.USER) {
	     conversationPage = conversationRepository.findAllByUser1OrUser2WithMessages(me, PageRequest.of(conversationPag, conversationSize));
	    if(conversationPage.getContent().isEmpty()) {
	    	throw new RecordNotFoundException("no conversation yet");
	    }
		}else {
			 conversationPage = conversationRepository.findAllByCompany1OrCompany2WithMessages(me, PageRequest.of(conversationPag, conversationSize));
			    if(conversationPage.getContent().isEmpty()) {
			    	throw new RecordNotFoundException("no conversation yet");
			    }
		}
	    List<ConversationDto> conversationDtos = new ArrayList<>();
	    List<MessageDto> messageDtos = new ArrayList<>();
	    for (Conversation conversation : conversationPage.getContent()) {
	    	String lastMessage = messageRepository.findLastMessageByConversationId(conversation.getId());
	        ConversationDto conversationDto = conversationMapper.mapToDto(conversation);
//	        Page<Message> messagePage = messageRepository.getMyMyLastMessage(1L, PageRequest.of(0, 1));
//	        for(Message mess : messagePage.getContent()) {
//	        	MessageDto dto = messageMapper.mapToDto(mess);
//	        	messageDtos.add(dto);	        	
//	        }
	        conversationDto.setMessage(lastMessage);
	        conversationDtos.add(conversationDto);
	        logger.warn("concversation size: "+conversationDtos.size()+" lastMessage : "+lastMessage);
	    }
	    return conversationDtos;
	}

	public ConversationDto getConversationByCaleeId(Long id, Long userId, Long companyId, MessageType messageType) {
		Optional<Conversation> conversation = null;
		if(userId != 0L) {
			if(messageType == MessageType.USER_SEND_COMPANY) {
				// me user company
				 conversation = conversationRepository.findByUser1IdOrUser2IdAndCompany1IdOrCompany2Id(userId, id);				
			}else {
				// me user user
				 conversation = conversationRepository.findByUser1IdAndUser2Id(userId, id);				
			}
		}else {
			if(messageType == MessageType.USER_SEND_COMPANY) {
				// me company user
				 conversation = conversationRepository.findByUser1IdOrUser2IdAndCompany1IdOrCompany2Id(id, companyId);				
			}else {
				// me company company
				 conversation = conversationRepository.findByCompany1IdAndCompany2Id(id, companyId);
			}
		}
		if(conversation.isEmpty()) {
			throw new RecordNotFoundException("this converstion does not exist");
		}
		ConversationDto conversationDto = conversationMapper.mapToDto(conversation.get());
		logger.warn("return conversation dto");
		return conversationDto;
	}

	public List<MessageDto> getAllMessageByCaleeId(Long id, Long userId, Long companyId, AccountType type, AccountType myAccount) {
		List<Message> messages = new ArrayList<>();
		if(myAccount == AccountType.COMPANY) {
			if(type == AccountType.COMPANY) {
				// me company company
				messages = messageRepository.findAllByReceiverCompanyIdAndSenderCompanyId(id,companyId);
			}else {
				// me company user
				messages = messageRepository.findAllByReceiverCompanyIdAndSenderUserId(companyId, id);
			}
			
		}else {
			if(type == AccountType.COMPANY) {
				// me user company
				messages = messageRepository.findAllByReceiverCompanyIdAndSenderUserId(id,userId);
			}else {
				// me user user
				messages = messageRepository.findAllByReceiverUserIdAndSenderUserId(id,userId);
			}
		}
		if(messages.isEmpty()) {
			throw new RecordNotFoundException("there is no message");
		}
		List<MessageDto> messagesDto = new ArrayList<>();
		for(Message i : messages) {
			MessageDto messageDto = messageMapper.mapToDto(i);
			messagesDto.add(messageDto);
		}
		logger.warn("return messages dto");
		return messagesDto;
	}

	public void sendMessageWithConversation(ConversationDto conversation, AccountType type, User user) {
	    Optional<Conversation> conv = conversationRepository.findById(conversation.getId());

	    if (conv.isPresent()) {
	        Conversation conversationEntity = conv.get();

	        boolean isUserPartOfConversation = 
	            (type == AccountType.USER && (
	                (conversationEntity.getUser1() != null && conversationEntity.getUser1().getId() == user.getId()) ||
	                (conversationEntity.getUser2() != null && conversationEntity.getUser2().getId() == user.getId())
	            ));

	        boolean isCompanyPartOfConversation = 
	            (type == AccountType.COMPANY && (
	                (conversationEntity.getCompany1() != null && conversationEntity.getCompany1().getUser().getId() == user.getId()) ||
	                (conversationEntity.getCompany2() != null && conversationEntity.getCompany2().getUser().getId() == user.getId())
	            ));

	        if (isUserPartOfConversation || isCompanyPartOfConversation) {
	            saveNewMessage(conv, conversation.getMessage(), null, null, null, null, null);
	        }
	    }
	}



}
