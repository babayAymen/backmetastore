package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Invetation;

public interface InvetationRepository extends BaseRepository<Invetation, Long> {

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@Query("SELECT i FROM Invetation i WHERE"
			+ " i.companySender.id = :companyId"
			+ " OR i.companyReceiver.id = :companyId"
			)
	Page<Invetation> findAllByClientIdOrCompanyIdOrUserId( Long companyId, Pageable pageable);

	Page<Invetation> findAllByClientId(Long id, Pageable pageable);
	
	void deleteByClientIdAndCompanySenderId(Long id, Long id2);

	void deleteByCompanyReceiverIdAndCompanySenderId(Long id, Long id2);
	

	void deleteByClientIdAndCompanyReceiverIdOrCompanySenderId(Long clientId, Long reciverId, Long senderId);



}


