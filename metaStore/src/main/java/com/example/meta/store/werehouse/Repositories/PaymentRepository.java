package com.example.meta.store.werehouse.Repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Payment;

public interface PaymentRepository extends BaseRepository<Payment, Long> {

	@Query("SELECT p FROM Payment p WHERE p.invoice.provider.id = :companyId OR p.invoice.client.id = :companyId OR p.invoice.person.id = :userId")
	List<Payment> findAllByCompanyIdOrClientId(Long companyId, Long userId);

	
}
