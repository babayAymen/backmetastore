package com.example.meta.store.aymen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.aymen.entity.ReglementForProvider;

public interface ReglementForProviderRepository extends BaseRepository<ReglementForProvider, Long> {

	Page<ReglementForProvider> findAllByPaymentForProviderPerDayId(Long paymentId, Pageable pageable);

}
