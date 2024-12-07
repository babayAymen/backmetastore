package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;

public interface CompanyRepository extends BaseRepository<Company, Long> {


	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	boolean existsByName(String name);

	boolean existsByUserId(Long id);
	
	Optional<Company> findByUserId(Long userId);
	
	boolean existsByCode(String code);

	boolean existsByMatfisc(String matfisc);
	
	boolean existsByBankaccountnumber(String bankaccountnumber);
	
	@Query("SELECT c FROM Company c WHERE c.name LIKE %:branshe% AND c.id <> :id")
	List<Company> findByNameContaining(String branshe, Long id);
	/////////////////////////////////////////////////////// not work ///////////////////////////////////////////////////
	void deleteByIdAndUserId(Long id, Long userId );
//
//	@Query("SELECT c FROM Company c JOIN c.providerCompany cr WHERE c.name LIKE %:search% OR c.code LIKE %:search% AND"
//			+ " (c.isVisible = 2 OR (c.isVisible = 1 AND cr.client.id = :clientId))")
	@Query("SELECT c FROM Company c WHERE"
			+ " (c.name LIKE %:search% OR c.code LIKE %:search%) AND "
			+ "  (c.isVisible = 2 OR"
			+ " (c.isVisible = 1 AND EXISTS (SELECT pc FROM ClientProviderRelation pc WHERE (pc.provider = c AND (pc.client.id = :clientId OR pc.person.id = :personId)))) "
			+ ")")
	List<Company> findAllContaining(String search, Long clientId, Long personId);
///////////////////////////////////////////////////////////////////////////////////////////////////////
	@Query("SELECT c FROM Company c WHERE"
			+ " (c.name LIKE %:search% OR c.code LIKE %:search%) AND"
			+ "  (c.isVisible = 2 OR"
			+ " (c.isVisible = 1 AND EXISTS (SELECT pc FROM ClientProviderRelation pc WHERE (pc.client = c AND pc.provider.id = :providerId))) "
			+ ")")
	List<Company> findAllByIsVisibleAndNameContainingOrCodeContaining(String search, Long providerId);

	@Query("SELECT c FROM Company c WHERE"
			+ " (c.name LIKE %:search% OR c.code LIKE %:search%) "
			+ " AND (c.isVisible = 2 "
			+ " OR (c.isVisible = 1 "
			+ " AND ((EXISTS (SELECT pc FROM ClientProviderRelation pc WHERE (pc.provider = c AND (pc.client.id = :companyId OR pc.person.id = :userId)))) "
			+ " OR ( EXISTS (SELECT pc FROM ClientProviderRelation pc WHERE (pc.client = c AND (pc.provider.id = :companyId OR pc.person.id = :userId)))))"
			+ ") "
			+ ")"
			)
	Page<Company> getAllCompaniesContaining(Long userId , Long companyId, String search, Pageable pageable);

	@Query("SELECT p FROM Company p JOIN p.clientCompany c WHERE (c.id = :companyId AND c.isDeleted = false) AND "
			+ "(p.name LIKE %:search% OR p.code LIKE %:search%)")
	List<Company> findAllMyByNameContainingOrCodeContainingAndProviderId(String search, Long companyId);

	

}


