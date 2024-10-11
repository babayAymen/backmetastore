package com.example.meta.store.werehouse.Controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ConversationDto;
import com.example.meta.store.werehouse.Dtos.MessageDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Conversation;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.MessageType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.MessageService;

import lombok.RequiredArgsConstructor;

@RestController()
@RequestMapping("/werehouse/message/")
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;

	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final JwtAuthenticationFilter authenticationFilter;

	private final Logger logger = LoggerFactory.getLogger(MessageController.class);
	
	@PostMapping("send")
	public void sendMessage(@RequestBody ConversationDto conversation) {
		if(conversation.getId() != null) {
			User user = userService.getUser();
			messageService.sendMessageWithConversation(conversation, authenticationFilter.accountType, user);
		}else {
		logger.warn(conversation.getId()+"conv id ");
		MessageType type = conversation.getType();
		logger.warn(type+" type is ");
		switch (type) {
		case COMPANY_SEND_COMPANY: {
			Company company = companyService.getCompany();
			Company company1 = companyService.getById(conversation.getCompany2().getId()).getBody();
			messageService.sendMessageCompanyCompany(conversation.getMessage(), company1,company,type);
			break;
		}
		case COMPANY_SEND_USER: {
			User receiver = userService.findById(conversation.getUser2().getId()).orElseThrow(() -> new RecordNotFoundException("this user does not exist"));
			Company company = companyService.getCompany();
			messageService.sendMessageCompanyUser(conversation.getMessage(), company, receiver,true,type);
			break;
		}
		case USER_SEND_COMPANY: {
			User user = userService.getUser();
			Company company = companyService.getById(conversation.getCompany2().getId()).getBody();
			messageService.sendMessageCompanyUser(conversation.getMessage(), company, user, false,type);
			break;
		}
		case USER_SEND_USER: {
			logger.warn("conversation id :"+type);
			User receiver = userService.findById(conversation.getUser2().getId()).orElseThrow(() -> new RecordNotFoundException("this user does not exist"));
			User user = userService.getUser();
			messageService.sendMessage(conversation.getMessage(), receiver,user,type);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
	} 
	}
	
	@GetMapping("get_message/{conversationId}")
	public List<MessageDto> getAllMyMessage(@PathVariable Long conversationId){
		logger.warn("c bon message con");
		AccountType type = authenticationFilter.accountType;
		Long id = null;
		if(type == AccountType.USER) {			
		User me = userService.getUser();
		id = me.getId();
		logger.warn(id+" id from user");
		}else {
			Company company = companyService.getCompany();
			id = company.getId();
			logger.warn(id+" id from company");
		}
		logger.warn(conversationId+" id from conversation");
		return messageService.getAllMyMessage(id,conversationId,type);
	}
	
	@GetMapping("getmessage/{id}/{type}")
	public List<MessageDto> getAllMessageByCaleeId(@PathVariable Long id , @PathVariable AccountType type){
		logger.warn("getAllMessageByCaleeId fun calee id "+id +" calee type : "+ type);
		AccountType myAccount = authenticationFilter.accountType;
		Long userId = 0L , companyId = 0L;
		if(myAccount == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			companyId = company.getId();
		}else {
			User user = userService.getUser();
			userId = user.getId();
		}
		return messageService.getAllMessageByCaleeId(id, userId, companyId, type, myAccount);
	}
	
	@GetMapping("get_conversation")
	public List<ConversationDto> getAllConversation(){
		AccountType type = authenticationFilter.accountType;
		Long id = 0L;
		if(type == AccountType.USER) {
		User user = userService.getUser();
		id = user.getId();
		}else {
		Company company = companyService.getCompany();
		id = company.getId();
		}
		return messageService.getAllMyConversation(id, 0, 10, 0, 10,type );
	}
	
	@GetMapping("getconversation/{id}/{messageType}")
	public ConversationDto getConversationByCaleeId(@PathVariable Long id, @PathVariable MessageType messageType) {
		AccountType type = authenticationFilter.accountType;
		Long userId = 0L , companyId = 0L;
		if(type == AccountType.USER) {
			User user = userService.getUser();
			userId = user.getId();
		}
		if(type == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			companyId = company.getId();
		}
		return messageService.getConversationByCaleeId(id,userId,companyId, messageType);
	}
}
