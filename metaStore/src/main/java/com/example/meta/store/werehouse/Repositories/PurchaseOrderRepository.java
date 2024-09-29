package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.PurchaseOrder;

public interface PurchaseOrderRepository extends BaseRepository<PurchaseOrder, Long>{

	List<PurchaseOrder> findAllByCompanyId(Long id);

	@Query("SELECT p FROM PurchaseOrder p WHERE (p.company.id = :companyId OR p.client.id = :companyId OR p.person.id = :userId)")
	List<PurchaseOrder> findAllByCompanyIdOrClientIdOrUserId(Long companyId, Long userId);

	@Query("SELECT MAX(p.orderNumber) FROM PurchaseOrder p WHERE (p.client.id = :clientId) OR (p.person.id = :userId)")
	Long getLastOrderNumber(Long clientId, Long userId);
	
}
