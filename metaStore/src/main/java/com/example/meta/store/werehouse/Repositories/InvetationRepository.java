package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Entities.Invetation;

public interface InvetationRepository extends BaseRepository<Invetation, Long> {

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@Query("SELECT i FROM Invetation i WHERE"
			+ " i.companySender.id = :companyId"
			+ " OR i.companyReciver.id = :companyId"
			+ " OR i.client.id = :clientId"
			)
	List<Invetation> findAllByClientIdOrCompanyIdOrUserId(Long clientId, Long companyId);

	void deleteByClientIdAndCompanySenderId(Long id, Long id2);

	void deleteByCompanyReciverIdAndCompanySenderId(Long id, Long id2);
	
//	@Query("SELECT i FROM Invetation i WHERE i.worker.id = :id")
//	Invetation findByWorkerId(Long id);

	void deleteByClientIdAndCompanyReciverIdOrCompanySenderId(Long clientId, Long reciverId, Long senderId);



}


