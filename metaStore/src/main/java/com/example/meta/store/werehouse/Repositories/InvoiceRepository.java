package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

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

	List<Invoice> findAllByProviderId(Long providerId);
	
	List<Invoice> findAllByClientId(Long clientId);
	
	@Query("SELECT i FROM Invoice i WHERE (i.client.id = :companyId) OR (i.provider.id = :companyId)")
	List<Invoice> findAllByClientIdOrProviderId( Long companyId);
	
	List<Invoice> findAllByPersonId( Long userId);

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

	Boolean existsByPersonIdAndProviderIdAndIsEnabledToComment(Long myUserId, Long providerId, boolean b);

	Boolean existsByClientIdAndProviderIdAndIsEnabledToComment(Long myCompanyId, Long providerId, boolean b);

	List<Invoice> findAllByPersonIdAndStatus(Long userId, Status status);

	List<Invoice> findAllByClientIdAndStatus(Long companyId, Status status);

	List<Invoice> findByProviderIdAndClientIdAndIsEnabledToComment(Long id, Long id2, Boolean isEnabled);

	List<Invoice> findByProviderIdAndPersonIdAndIsEnabledToComment(Long id, Long id2, Boolean isEnabled);

	List<Invoice> findByProviderIdAndPaid(Long companyId, PaymentStatus status);

	List<Invoice> findByProviderIdAndStatus(Long companyId, Status status);







}
