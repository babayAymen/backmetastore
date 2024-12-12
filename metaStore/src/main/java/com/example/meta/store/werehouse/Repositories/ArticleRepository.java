package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Enums.CompanyCategory;

public interface ArticleRepository extends BaseRepository<Article, Long>{

	
	@Query("SELECT a FROM Article a WHERE a.category = :category"
			+ " AND NOT EXISTS (SELECT ac FROM ArticleCompany ac WHERE ac.article = a AND ac.company.id = :companyId AND ac.isDeleted = false)"
			)
	Page<Article> finAllByCategoryAndCompanyId(CompanyCategory category, Long companyId, Pageable pageable );
	




}
 