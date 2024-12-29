package com.example.meta.store.PointsPayment.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviderPerDay;

public interface PaymentForProviderPerDayRepository extends BaseRepository<PaymentForProviderPerDay, Long>{
	
	
	@Query("SELECT p FROM PaymentForProviderPerDay p WHERE p.receiver.id = :id AND FUNCTION('DATE', p.createdDate) = :now")
	Optional<PaymentForProviderPerDay> findByProviderIdAndCreatedDate(@Param("id") Long id, @Param("now") LocalDate now);


    @Query("SELECT SUM(p.amount) FROM PaymentForProviderPerDay p WHERE p.receiver.id = :id AND p.lastModifiedDate BETWEEN :startDate AND :endDate")
    BigDecimal getSumAmountByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Long id);


	Page<PaymentForProviderPerDay> findByReceiverId(Long id, Pageable pageable);


	@Query("SELECT p FROM PaymentForProviderPerDay p WHERE p.receiver.id = :id AND p.lastModifiedDate BETWEEN :date AND :findate")
	Page<PaymentForProviderPerDay> findByProviderIdAndDate(LocalDateTime date, LocalDateTime findate, Long id, Pageable pageable);
}

