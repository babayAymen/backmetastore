package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;

public interface ClientProviderRelationRepository extends BaseRepository<ClientProviderRelation, Long>{

	boolean existsByPersonIdAndProviderId(Long id, Long id2);
//
//	@Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM ClientProviderRelation re "
//			+ "WHERE (re.client.id = :clientId AND re.provider.id = :providerId) OR"
//			+ " (re.client.id = :providerId AND re.provider.id = :clientId)) THEN 1 ELSE 0 END")
	boolean existsByClientIdAndProviderId(Long clientId, Long providerId);

	Optional<ClientProviderRelation> findByProviderIdAndClientIdAndIsDeletedFalse(Long id, Long id2);
	
	Optional<ClientProviderRelation> findByProviderIdAndPersonId(Long id, Long id2);

	void deleteByProviderIdAndClientId(Long id, Long id2);

	Page<ClientProviderRelation> findAllByClientIdAndIsDeletedFalseAndIsDeletedFalse(Long companyId, Pageable pageable);

	@Query("SELECT p FROM ClientProviderRelation p WHERE (p.client.id = :companyId OR p.person.id = :userId) AND p.isDeleted = false AND "
			+ "(p.provider.name LIKE %:search% OR p.provider.code LIKE %:search%)")
	List<ClientProviderRelation> findAllMyByNameContainingOrCodeContainingAndProviderId(String search, Long companyId, Long userId);

	@Query("SELECT p FROM ClientProviderRelation p WHERE p.provider.isVirtual = true AND p.client.id = :companyId")
	List<ClientProviderRelation> findAllByCompanyIdAndIsVirtualTrue(Long companyId);

	Optional<ClientProviderRelation> findByProviderIdAndClientIdAndIsDeletedTrue(Long providerId, Long clientId);

	@Query("SELECT r FROM ClientProviderRelation r WHERE (r.client.id = :clientId AND r.provider.id = :providerId) OR"
			+ " (r.client.id = :providerId AND r.provider.id = :clientId)")
	Optional<ClientProviderRelation> findByClientIdAndProviderId(Long clientId, Long providerId);
//	
//	@Query("SELECT r FROM ClientProviderRelation r WHERE (r.client.id = :id1 AND r.provider.id = :id2) OR (r.client.id = :id2 AND r.provider.id = :id1) ")
//	Optional<ClientProviderRelation> findByClientIdAndProviderIdOrProviderIdAndClientId(Long id1, Long id2);

	void deleteByClientIdAndProviderId(Long id, Long id2);

	void deleteByPersonIdAndProviderId(Long id, Long id2);

	Page<ClientProviderRelation> getAllByProviderIdAndIsDeletedFalse(Long id, Pageable pageable);


	@Query("SELECT p FROM ClientProviderRelation p WHERE (p.provider.id = :companyId) AND (p.isDeleted = false) AND "
			+ "(p.client.name LIKE %:search% OR p.client.code LIKE %:search%)")
	List<ClientProviderRelation> findAllMyByNameContainingOrCodeContainingAndClientId(String search, Long companyId);

	@Query("SELECT p FROM ClientProviderRelation p WHERE (p.person.id = :userId) AND (p.isDeleted = false) AND "
			+ "(p.provider.name LIKE %:search% OR p.provider.code LIKE %:search%)")
	List<ClientProviderRelation> findAllMyByNameContainingOrCodeContainingAndPersonId(String search, Long userId);
	
	
	@Query("SELECT p.provider FROM ClientProviderRelation p WHERE (p.person.id = :userId AND p.isDeleted = false) AND "
			+ "(p.provider.name LIKE %:search% OR p.provider.code LIKE %:search%)")
	List<Company> findAllMyByNameContainingOrCodeContaining(String search, Long userId);
	
	
	@Query("SELECT r FROM ClientProviderRelation r " +
		       "WHERE r.provider.id = :id AND r.isDeleted = false AND " +
		       "(" +
		       "(r.client IS NOT NULL AND LOWER(r.client.name) LIKE LOWER(CONCAT('%', :search, '%')))" +
		       ")")
		Page<ClientProviderRelation> findByMyCompanyAndClientContaining(@Param("search") String search, @Param("id") Long id, Pageable pageable);


	@Query("SELECT r FROM ClientProviderRelation r " +
		       "WHERE r.provider.id = :id AND r.isDeleted = false AND " +
		       "(" +
		       "(r.person IS NOT NULL AND LOWER(r.person.username) LIKE LOWER(CONCAT('%', :search, '%')))  " +
		       ")")
		Page<ClientProviderRelation> findByMyCompanyAndUserContaining(@Param("search") String search, @Param("id") Long id, Pageable pageable);

	
	@Query("SELECT r.person FROM ClientProviderRelation r " +
		       "WHERE r.provider.id = :id AND r.isDeleted = false AND " +
		       "(" +
		       "(r.person IS NOT NULL AND LOWER(r.person.username) LIKE LOWER(CONCAT('%', :search, '%')))  " +
		       ")")
		List<User> findByMyUserContaining(@Param("search") String search, @Param("id") Long id);
	

	Optional<ClientProviderRelation> findByPersonIdAndProviderId(Long clientId, Long id);
	
	Optional<ClientProviderRelation> findByClientIdOrProviderId(Long companyId,Long providerId);
	
	@Query("SELECT r.client FROM ClientProviderRelation r WHERE (r.provider.id = :id) AND (r.isDeleted = false)"
			+ " AND (r.client.name LIKE %:search% OR r.client.code LIKE %:search%)")
	Page<Company> findAllByProviderId(Long id, String search, Pageable pageable);

	@Query("SELECT r.provider FROM ClientProviderRelation r WHERE (r.client.id = :id) AND (r.isDeleted = false)"
			+ " AND (r.provider.name LIKE %:search% OR r.provider.code LIKE %:search%)")
	Page<Company> findAllByClientId(Long id, String search, Pageable pageable);
	
	@Query("SELECT r.person FROM ClientProviderRelation r WHERE r.provider.id = :id AND r.person.username LIKE %:search% ")
	Page<User> findByUserNameContaining(Long id, String search, Pageable pageable);
	

	
}
