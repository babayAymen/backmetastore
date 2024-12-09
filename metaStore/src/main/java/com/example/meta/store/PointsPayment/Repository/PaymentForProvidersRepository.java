package com.example.meta.store.PointsPayment.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviders;
import com.example.meta.store.werehouse.Entities.Payment;

public interface PaymentForProvidersRepository extends BaseRepository<PaymentForProviders, Long> {

	
//	@Query("SELECT p FROM PaymentForProviders p "
//			+ "JOIN p.purchaseOrderLine l "
//			+ "JOIN l.purchaseorder o  "
//			+ "WHERE o.client.id = :me OR o.company.id = :me")
//	Page<PaymentForProviders> getPaymentForProvidersAsCompany(Long me, Pageable pagebale);
//	
	
	@Query("SELECT p FROM PaymentForProviders p "
		       + "JOIN p.purchaseOrderLine l "
		       + "JOIN l.purchaseorder o "
		       + "WHERE o.client.id = :me OR o.company.id = :me "
		       + "ORDER BY p.purchaseOrderLine ASC")
		Page<PaymentForProviders> getPaymentForProvidersAsCompany(Long me, Pageable pageable);

	
	@Query("SELECT p FROM PaymentForProviders p "
			+ "JOIN p.purchaseOrderLine l "
			+ "JOIN l.purchaseorder o  "
			+ "WHERE o.person.id = :me"
			)
	Page<PaymentForProviders> getPaymentForProvidersAsUser(Long me, Pageable pageable);

	@Query("SELECT p FROM PaymentForProviders p WHERE p.purchaseOrderLine.purchaseorder.company.id = :id AND (p.createdDate BETWEEN :date AND :date2)")
	Page<PaymentForProviders> findByCreatedDate(LocalDateTime date, LocalDateTime date2, Long id, Pageable pageable);

	
}
