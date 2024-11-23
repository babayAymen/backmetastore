package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;
import com.example.meta.store.werehouse.Enums.Status;

public interface PurchaseOrderLineRepository extends BaseRepository<PurchaseOrderLine, Long>{

	@Query("SELECT p FROM PurchaseOrderLine p WHERE p.id = :id AND (p.purchaseorder.client.id = :clientId OR p.purchaseorder.person.id = :userId)")
	Optional<PurchaseOrderLine> findByIdAndClientIdOrUserId(Long id, Long clientId, Long userId);

	@Query("SELECT p FROM PurchaseOrderLine p WHERE (p.purchaseorder.company.id = :companyId OR p.purchaseorder.client.id = :companyId OR p.purchaseorder.person.id = :personId)")
	List<PurchaseOrderLine> findAllByCompanyIdOrClientIdOrPclientId(Long companyId, Long personId);

	
	List<PurchaseOrderLine> findAllByPurchaseorderId(Long id);

	@Query("SELECT p FROM PurchaseOrderLine p WHERE p.invoice.id = :invoiceId AND p.purchaseorder.company.id = :companyId")
	List<PurchaseOrderLine> findAllByInvoiceIdAndCompanyId(Long invoiceId, Long companyId);

	@Query("SELECT p FROM PurchaseOrderLine p WHERE p.invoice.id = :invoiceId AND p.purchaseorder.person.id = :userId")
	List<PurchaseOrderLine> findByInvoiceIdAndPersonId(Long invoiceId, Long userId);

	@Query("SELECT p FROM PurchaseOrderLine p WHERE p.purchaseorder.person.id = :id AND p.status = :status")
	Page<PurchaseOrderLine> findAllNotAcceptedAsClient(Long id, Status status, Pageable pageable);

	@Query("SELECT p FROM PurchaseOrderLine p WHERE p.purchaseorder.company.id = :id AND p.status = :status")
	Page<PurchaseOrderLine> findAllNotAcceptedAsProvider(Long id, Status status, Pageable pageable);



}
