package com.example.meta.store.PointsPayment.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.PointsPayment.Entity.PointsPayment;

public interface PointPaymentRepository extends BaseRepository<PointsPayment, Long>{

	@Query("SELECT p FROM PointsPayment p WHERE p.clientCompany.id = :companyId OR p.provider.id = :companyId OR p.clientUser.id = :userId")
	List<PointsPayment> findAllByCompanyIdOrUserId(Long companyId, Long userId);

}
