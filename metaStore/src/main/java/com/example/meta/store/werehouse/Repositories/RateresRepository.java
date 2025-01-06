package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Raters;

public interface RateresRepository extends BaseRepository<Raters, Long>{

	Page<Raters> findAllByRateeCompanyId(Long id, Pageable pageable);

	Page<Raters> findAllByRateeUserId(Long id, Pageable pageable);

}
