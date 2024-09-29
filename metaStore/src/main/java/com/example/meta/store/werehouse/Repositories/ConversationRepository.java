package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Conversation;

public interface ConversationRepository extends BaseRepository<Conversation, Long>{

	@Query("SELECT c FROM Conversation c WHERE (c.user1 = :user1 AND c.user2 = :user2) Or (c.user1 = :user2 AND c.user2 = :user1)")
	Optional<Conversation> findAllByUser1AndUser2(User user1, User user2);

	@Query("SELECT c FROM Conversation c WHERE c.user1 = :me OR c.user2 = :me")
	List<Conversation> findAllByUser1OrUser2(User me);

	


    @Query("SELECT c FROM Conversation c " +
           "WHERE c.user1.id = :me OR c.user2.id = :me "
           )
     Page<Conversation> findAllByUser1OrUser2WithMessages(Long me, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "WHERE c.company1.id = :me OR c.company2.id = :me "
            )
      Page<Conversation> findAllByCompany1OrCompany2WithMessages(Long me, Pageable pageable);

    
    @Query("SELECT c FROM Conversation c WHERE (c.company1 = :receiver AND c.company2 = :me) OR (c.company1 = :me AND c.company2 = :receiver)")
	Optional<Conversation> findAllByCompany1AndCompany2(Company receiver, Company me);

    @Query("SELECT c FROM Conversation c WHERE (c.company1 = :receiver OR c.company2 = :receiver) AND (c.user1 = :me OR c.user2 = :me)")
	Optional<Conversation> findAllByCompany1AndUser2(Company receiver, User me);

    
    
    
    
    @Query("SELECT c FROM Conversation c WHERE (c.user1.id = :userId OR c.user2.id = :userId) AND (c.company1.id = :id OR c.company2.id = :id)")
	Optional<Conversation> findByUser1IdOrUser2IdAndCompany1IdOrCompany2Id(Long userId, Long id);

	
	@Query("SELECT c FROM Conversation c WHERE (c.user1.id = :userId AND c.user2.id = :id) OR (c.user1.id = :id AND c.user2.id = :userId)")
	Optional<Conversation> findByUser1IdAndUser2Id(Long userId, Long id);

	
	@Query("SELECT c FROM Conversation c WHERE (c.company1.id = :companyId AND c.company2.id = :id) OR (c.company1.id = :id AND c.company2.id = :companyId)")
	Optional<Conversation> findByCompany1IdAndCompany2Id(Long id, Long companyId);

}
