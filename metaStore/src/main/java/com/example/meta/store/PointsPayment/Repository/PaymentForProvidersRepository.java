package com.example.meta.store.PointsPayment.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviders;
import com.example.meta.store.werehouse.Entities.Payment;

public interface PaymentForProvidersRepository extends BaseRepository<PaymentForProviders, Long> {

	
	@Query("SELECT p FROM PaymentForProviders p "
			+ "JOIN p.purchaseOrderLine l "
			+ "JOIN l.purchaseorder o  "
			+ "WHERE o.client.id = :me OR o.company.id = :me")
	List<PaymentForProviders> getPaymentForProvidersAsCompany(Long me);
	
	@Query("SELECT p FROM PaymentForProviders p "
			+ "JOIN p.purchaseOrderLine l "
			+ "JOIN l.purchaseorder o  "
			+ "WHERE o.person.id = :me"
			)
	List<PaymentForProviders> getPaymentForProvidersAsUser(Long me);

	@Query("SELECT p FROM PaymentForProviders p WHERE p.purchaseOrderLine.purchaseorder.company.id = :id AND (p.createdDate BETWEEN :date AND :date2)")
	List<PaymentForProviders> findByCreatedDate(LocalDateTime date, LocalDateTime date2, Long id);

	
}
