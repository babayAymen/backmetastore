package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Message;

public interface MessageRepository extends BaseRepository<Message, Long>{

	@Query("SELECT m FROM Message m WHERE "
			+ "(m.conversation.user1.id = :receiver AND m.conversation.user2.id = :sender)"
			+ " OR (m.conversation.user2.id = :receiver AND m.conversation.user1.id = :sender)")
	List<Message> findAllBySenderAndReceiver(Long receiver, Long sender);

//	  @Query("SELECT m FROM Message m " +
//	           "WHERE m.az = :conversationId " +
//	           "AND m.id = (SELECT MAX(m2.id) FROM Message m2 WHERE m2.az = :conversationId)")
//	  Message getMyMyLastMessage(Long conversationId);

	
	@Query("SELECT m FROM Message m " +
	           "WHERE m.conversation.id = :conversationId " +
	           "ORDER BY m.id DESC")
	    Page<Message> getMyMyLastMessage(@Param("conversationId") Long conversationId, Pageable pageSize);

	@Query("SELECT m.content FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdDate DESC LIMIT 1")
	String findLastMessageByConversationId(Long conversationId);

	@Query("SELECT m FROM Message m WHERE"
			+ " m.conversation.id = :conversationId"
			+ " AND (m.conversation.user2.id = :me OR m.conversation.user1.id = :me)")
	List<Message> findAllByMeAsUserAndConversationId(Long me, Long conversationId);

	@Query("SELECT m FROM Message m WHERE"
			+ " m.conversation.id = :conversationId"
			+ " AND (m.conversation.company1.id = :me OR m.conversation.company2.id = :me)")
	List<Message> findAllByMeAsCompanyAndConversationId(Long me, Long conversationId);

	
	@Query("SELECT m FROM Message m WHERE"
			+ " (m.conversation.company1.id = :id AND m.conversation.company2.id = :companyId)"
			+ " OR (m.conversation.company2.id = :id AND m.conversation.company1.id = :companyId)")
	List<Message> findAllByReceiverCompanyIdAndSenderCompanyId(Long id, Long companyId);

	@Query("SELECT m FROM Message m WHERE"
			+ " (m.conversation.company1.id = :companyId AND"
			+ " (m.conversation.user1.id = :id OR m.conversation.user2.id = :id)) OR"
			+ " (m.conversation.company2.id = :companyId AND (m.conversation.user1.id = :id OR m.conversation.user2.id = :id))")
	List<Message> findAllByReceiverCompanyIdAndSenderUserId(Long companyId, Long id);

	@Query("SELECT m FROM Message m WHERE"
			+ " (m.conversation.user1.id = :id AND m.conversation.user2.id = :userId)"
			+ " OR (m.conversation.user1.id = :userId AND m.conversation.user2.id = :id)")
	List<Message> findAllByReceiverUserIdAndSenderUserId(Long id, Long userId);


}

