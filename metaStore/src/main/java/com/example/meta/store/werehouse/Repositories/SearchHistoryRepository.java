package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.SearchHistory;
import com.example.meta.store.werehouse.Enums.SearchCategory;

public interface SearchHistoryRepository extends BaseRepository<SearchHistory, Long> {

	@Query("SELECT s FROM SearchHistory s WHERE s.searchCategory = :name and s.article.id = :itemId and (s.meUser.id = :id OR s.meCompany.id = :companyId)")
	Optional<SearchHistory> existsByArticleAndSearchCategoryAndSearcher(Long itemId, SearchCategory name, Long id, Long companyId);

	@Query("SELECT s FROM SearchHistory s WHERE s.searchCategory = :name and s.company.id = :itemId and (s.meUser.id = :id OR s.meCompany.id = :companyId)")
	Optional<SearchHistory> existsByCompanyAndSearchCategoryAndSearcher(Long itemId, SearchCategory name, Long id, Long companyId);

	@Query("SELECT s FROM SearchHistory s WHERE s.searchCategory = :name and s.user.id = :itemId and (s.meUser.id = :id OR s.meCompany.id = :companyId)")
	Optional<SearchHistory> existsByUserAndSearchCategoryAndSearcher(Long itemId, SearchCategory name, Long id, Long companyId);

	List<SearchHistory> findAllByCreatedBy(Long id);

	Page<SearchHistory> findAllByMeUserId(Long id, Pageable pageable);

	Page<SearchHistory> findAllByMeCompanyId(Long id, Pageable pageable);

	Page<SearchHistory> findAllByMeCompanyIdAndLastModifiedBy(Long meCompanyId, Long lastModifiedBy, Pageable pageable);

}
