package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.Status;

public interface InvoiceRepository extends BaseRepository<Invoice, Long> {

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@Query("SELECT i FROM Invoice i WHERE i.provider.id = :companyId AND i.code = (SELECT max(i2.code) FROM Invoice i2 WHERE i2.provider.id = :companyId)")
	Optional<Invoice> lastInvoice(Long companyId);

	Optional<Invoice> findByCodeAndClientId(Long code, Long clientId);

	Optional<Invoice> findByCodeAndPersonId(Long code, Long clientId);

	Page<Invoice> findAllByProviderId(Long providerId, Pageable pageable);
	
	Page<Invoice> findAllByClientId(Long clientId, Pageable pageable);
	
	@Query("SELECT i FROM Invoice i WHERE (i.client.id = :companyId) OR (i.provider.id = :companyId)")
	List<Invoice> findAllByClientIdOrProviderId( Long companyId);
	
	Page<Invoice> findAllByPersonId( Long userId, Pageable pageable);

	/////////////////////////////////////////////////////// not work ///////////////////////////////////////////////////
	Optional<Invoice> findByCodeAndProviderId(Long code, Long companyId);

	@Query("SELECT a.code FROM Invoice a WHERE a.provider.id = :companyId")
	List<Long> findAllByCompany(@Param("companyId")Long companyId);

	Optional<Invoice> findByIdAndProviderId(Long id, Long companyId);

	@Query("SELECT max(code) FROM Invoice i WHERE i.provider.id = :companyId ")
	Long max(Long companyId);
	
	
	@Query("SELECT a.code FROM Invoice a WHERE a.client.id = :clientId")
	List<Long> findByClientId(Long clientId);
	
	static boolean existsByClientId(Long id) {
		return false;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////

//	Boolean existsByPersonIdAndProviderIdAndIsEnabledToComment(Long myUserId, Long providerId, boolean b);
//
//	Boolean existsByClientIdAndProviderIdAndIsEnabledToComment(Long myCompanyId, Long providerId, boolean b);

	Boolean existsByPersonIdAndProviderId(Long myUserId, Long providerId);

	Boolean existsByClientIdAndProviderId(Long myCompanyId, Long providerId);
	
	Page<Invoice> findAllByPersonIdAndStatus(Long userId, Status status, Pageable pageable);

	Page<Invoice> findAllByClientIdAndStatus(Long companyId, Status status, Pageable pageable);

//	List<Invoice> findByProviderIdAndClientIdAndIsEnabledToComment(Long id, Long id2, Boolean isEnabled);

	List<Invoice> findByProviderIdAndClientId(Long id, Long id2);

//	List<Invoice> findByProviderIdAndPersonIdAndIsEnabledToComment(Long id, Long id2, Boolean isEnabled);
	List<Invoice> findByProviderIdAndPersonId(Long id, Long id2);

	Page<Invoice> findByProviderIdAndPaid(Long companyId, PaymentStatus status, Pageable pageable);

	Page<Invoice> findByProviderIdAndStatus(Long companyId, Status status, Pageable pageable);







}
