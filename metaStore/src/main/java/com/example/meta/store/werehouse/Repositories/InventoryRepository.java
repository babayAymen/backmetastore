package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Inventory;

public interface InventoryRepository extends BaseRepository<Inventory, Long>{

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@Query("SELECT i FROM Inventory i WHERE i.company.id = :companyId AND i.article.isDeleted = false")
	Page<Inventory> findByCompanyIdAndIsDeleteFalse(Long companyId, Pageable pageable);
	
//	@Query("SELECT i FROM Inventory i WHERE i.article.id = :companyArticle AND i.company.id = :id")
	Optional<Inventory> findByArticleIdAndCompanyId(Long companyArticle, Long id);

	

}
