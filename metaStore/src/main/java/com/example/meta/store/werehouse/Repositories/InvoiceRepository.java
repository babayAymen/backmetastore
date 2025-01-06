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
	
	Page<Invoice> findAllByProviderIdAndPaid(Long providerId , PaymentStatus status , Pageable pageable);
	
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
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	Boolean existsByPersonIdAndProviderId(Long myUserId, Long providerId);

	Boolean existsByClientIdAndProviderId(Long myCompanyId, Long providerId);
	
	Page<Invoice> findAllByPersonIdAndStatus(Long userId, Status status, Pageable pageable);// i thing its unused

	Page<Invoice> findAllByClientIdAndStatus(Long companyId, Status status, Pageable pageable);


	List<Invoice> findByProviderIdAndClientId(Long id, Long id2);

	List<Invoice> findByProviderIdAndPersonId(Long id, Long id2);

	Page<Invoice> findByProviderIdAndPaid(Long companyId, PaymentStatus status, Pageable pageable);

	Page<Invoice> findByProviderIdAndStatus(Long companyId, Status status, Pageable pageable);

	Page<Invoice> findByClientIdAndPaid(Long companyId, PaymentStatus status, Pageable pageable);

	Page<Invoice> findAllByClientIdAndPaid(Long id, PaymentStatus status, Pageable pageable);

	Page<Invoice> findByProviderIdAndLastModifiedBy(Long companyId, Long workerId, Pageable pageable);

	@Query("SELECT i FROM Invoice i WHERE i.provider.id = :id AND  CAST(i.code AS text) LIKE %:text% ")
	Page<Invoice> findByInvoiceContaint(Long id, Long text, Pageable pageable);







}
