package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Raters;

public interface RateresRepository extends BaseRepository<Raters, Long>{

	List<Raters> findAllByRateeCompanyId(Long id);

	List<Raters> findAllByRateeUserId(Long id);

}
