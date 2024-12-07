package com.example.meta.store.werehouse.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.CompanyCategory;

public interface ArticleCompanyRepository extends BaseRepository<ArticleCompany, Long>{

	
//	@Query(value = "SELECT a FROM ArticleCompany a WHERE "
//			+ " ((a.isVisible = 2)"
//			+ " AND (a.company.longitude BETWEEN :longitude - 0.057615 AND :longitude + 0.057615) "
//		    + " AND (a.company.latitude BETWEEN :latitude - 0.042907 AND :latitude + 0.042907)) "
//			+ " ORDER BY random() LIMIT 10 "
//			)
	@Query(value = "SELECT a FROM ArticleCompany a WHERE"
			+ " (((a.isVisible = 2)"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE"
			+ "  cc.person.id = :userId AND cc.provider.id = a.company.id))))"
			+ " AND (a.company.longitude BETWEEN :longitude - 0.057615 AND :longitude + 0.057615) "
		    + " AND (a.company.latitude BETWEEN :latitude - 0.042907 AND :latitude + 0.042907) "
		   	)
	Page<ArticleCompany> findRandomArticles(Long userId, Double longitude, Double latitude, Pageable pageable );

	@Query(value = "SELECT a FROM ArticleCompany a WHERE"
			+ " (a.company.id = :myCompanyId) "
			+ " OR (((a.isVisible = 2)"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE"
			+ "  cc.client.id = :myCompanyId AND cc.provider.id = a.company.id))))"
			+ " AND (a.company.longitude BETWEEN :longitude - 0.057615 AND :longitude + 0.057615) "
		    + " AND (a.company.latitude BETWEEN :latitude - 0.042907 AND :latitude + 0.042907) "
		   	)
	Page<ArticleCompany> findRandomArticlesPro( Long myCompanyId, Double longitude, Double latitude, Pageable pageable);
	
	@Query("SELECT a FROM ArticleCompany a WHERE ((a.isVisible = 2) "
			+ " OR (a.isVisible = 1 AND ((EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE"
			+ " (cc.person.id = :clientId AND cc.client.id = :companyId) OR"
			+ " (cc.provider.id = :companyId))))))"
			+ " AND (a.company.id = :providerId) ")
	Page<ArticleCompany> findAllByCompanyId(Long companyId,Long clientId, Long providerId, Pageable pageable);


	@Query("SELECT a FROM ArticleCompany a WHERE a.article.code LIKE %:code% AND a.provider.id = :id")
	Optional<ArticleCompany> findByCodeAndProviderId(String code, Long id);

	Page<ArticleCompany> findAllByCompanyIdOrderByCreatedDateDesc(Long id, Pageable pageable);

	@Query("SELECT a FROM ArticleCompany a WHERE (a.category.id = :categoryId AND a.company.id = :companyId)"
			+ " AND (a.isVisible = 2"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE cc.client.id = :myClientId AND cc.provider.id = :companyId)))"
			)
	List<ArticleCompany> findAllByCategoryIdAndCompanyId(Long categoryId, Long companyId , Long myClientId);


	@Query("SELECT a FROM ArticleCompany a WHERE (a.subCategory.id = :subcategoryId AND a.company.id = :companyId)"
			+ " AND (a.isVisible = 2"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE cc.client.id = :myClientId AND cc.provider.id = :companyId)))"
			)
	List<ArticleCompany> findAllBySubCategoryIdAndCompanyId( Long subcategoryId, Long companyId , Long myClientId);

	
	List<ArticleCompany> findAllMyByCategoryIdAndCompanyId(Long categoryId, Long id);

	List<ArticleCompany> findAllMyBySubCategoryIdAndCompanyId(Long subcategoryId, Long id);
	
	@Query("SELECT a FROM ArticleCompany a WHERE "
			+ " (a.article.libelle LIKE %:libelle% OR a.article.code LIKE %:libelle%)"
			+ " AND (a.isVisible = 2"
			+ " OR (a.isVisible = 1"
			+ " AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE"
			+ " (cc.client.id = :providerId OR cc.provider.id = :providerId OR cc.person.id = :userId))))"
			)
	Page<ArticleCompany> findAllByLibelleAndProviderIdContaining(String libelle, Long providerId, Long userId, Pageable pageable);

	@Query("SELECT a FROM ArticleCompany a WHERE "
			+ " (a.article.libelle LIKE %:libelle% OR a.article.code LIKE %:libelle%)"
			+ " AND a.company.id = :providerId"
			)
	List<ArticleCompany> findAllMyByLibeleContaining(String libelle, Long providerId);

	
	@Query("SELECT a FROM ArticleCompany a WHERE (a.category.id = :categoryId)"
			+ " AND (a.isVisible = 2"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE cc.person.id = :clientId AND cc.provider.id = a.company.id)))"
			)
	List<ArticleCompany> findAllByCategoryIdAndPersonId(Long categoryId, Long clientId);


	@Query("SELECT a FROM ArticleCompany a WHERE (a.subCategory.id = :subcategoryId AND a.company.id = :companyId)"
			+ " AND (a.isVisible = 2"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE cc.person.id = :clientId AND cc.provider.id = :companyId)))"
			)
	List<ArticleCompany> findAllBySubCategoryIdAndPersonId(Long subcategoryId, Long companyId, Long clientId);

	Boolean existsByIdAndUsersId(Long id, Long userId);

	Boolean existsByIdAndCompaniesId(Long id, Long companyId);
	
	Boolean existsByArticleIdAndCompanyId(Long articleId, Long companyId);

	@Query(value = "SELECT a FROM ArticleCompany a WHERE"
			+ " (a.company.category = :categname) "
			+ " AND (((a.isVisible = 2)"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE"
			+ "  cc.client.id = :companyId AND cc.provider.id = a.company.id))))"
			+ " AND (a.company.longitude BETWEEN :longitude - 0.057615 AND :longitude + 0.057615) "
		    + " AND (a.company.latitude BETWEEN :latitude - 0.042907 AND :latitude + 0.042907) "
		   	+ " ORDER BY random() LIMIT 10 ")
	List<ArticleCompany> findAllByCompanyCategoryAndCompanyId(CompanyCategory categname, Long companyId, Double latitude , Double longitude);

	@Query(value = "SELECT a FROM ArticleCompany a WHERE"
			+ " (a.company.category = :categname) "
			+ " AND (((a.isVisible = 2)"
			+ " OR (a.isVisible = 1 AND EXISTS (SELECT 1 FROM ClientProviderRelation cc WHERE"
			+ "  cc.person.id = :userId AND cc.provider.id = a.company.id))))"
			+ " AND (a.company.longitude BETWEEN :longitude - 0.057615 AND :longitude + 0.057615) "
		    + " AND (a.company.latitude BETWEEN :latitude - 0.042907 AND :latitude + 0.042907) "
		   	+ " ORDER BY random() LIMIT 10 ")	
	List<ArticleCompany> findAllByCompanyCategoryAndUserId(CompanyCategory categname, Long userId, Double latitude , Double longitude);

	@Query("SELECT a FROM ArticleCompany a WHERE a.company.id = :id AND a.article.barcode = :barcode")
	ArticleCompany findByBarcodeAndCompanyId(String barcode, Long id);

	@Query("SELECT a FROM ArticleCompany a WHERE a.company.id = :id AND a.article.libelle LIKE %:search%")
	Page<ArticleCompany> findAllByCompanyIdAndLibelleContaining(Long id, String search, Pageable pageable);

	Page<ArticleCompany> findByCompanyId(Long companyId, Pageable pageable);
	

	
	
	
}
