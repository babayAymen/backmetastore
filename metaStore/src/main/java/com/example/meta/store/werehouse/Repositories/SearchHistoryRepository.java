package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

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

	List<SearchHistory> findAllByMeUserId(Long id);

	List<SearchHistory> findAllByMeCompanyId(Long id);

}
