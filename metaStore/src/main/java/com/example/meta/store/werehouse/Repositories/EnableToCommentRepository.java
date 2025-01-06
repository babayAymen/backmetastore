package com.example.meta.store.werehouse.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.EnableToComment;

public interface EnableToCommentRepository extends BaseRepository<EnableToComment, Long> {

	@Query(" SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END"
			+ "    FROM EnableToComment e"
			+ "    WHERE (e.raterCompany.id = :myCompanyId"
			+ "      AND e.rateeCompany.id = :companyId"
			+ "      AND e.enableClientCompany = :enable_client_company) "
			+ " OR (e.raterCompany.id = :companyId AND e.rateeCompany.id = :myCompanyId AND e.enableProvider = :enable_client_company)"
			)
	Boolean existsByRaterCompanyIdAndRateeCompanyIdAndEnableClientCompany(Long myCompanyId, Long companyId, boolean enable_client_company);
//	Boolean existsByRaterCompanyIdAndRateeCompanyIdAndEnableProvider(Long companyId , Long myCompanyId, boolean enable);
	Boolean existsByUserIdAndRateeCompanyIdAndEnableClientCompany(Long myCompanyId, Long companyId, boolean enable_client_company);
	Boolean existsByRaterCompanyIdOrRateeCompanyIdAndUserId(Long raterCompanyId,Long rateeCompanyId, Long userId);
	@Query("SELECT e FROM EnableToComment e "
			+ "    WHERE (e.raterCompany.id = :myCompanyId AND e.rateeCompany.id = :companyId AND e.enableClientCompany = :enable_client_company) "
			+ " OR (e.raterCompany.id = :companyId AND e.rateeCompany.id = :myCompanyId AND e.enableProvider = :enable_client_company)"
			+"ORDER BY e.id ASC LIMIT 1")
	Optional<EnableToComment> findByRaterCompanyIdAndRateeCompanyIdAndEnableClientCompany(Long myCompanyId, Long companyId,boolean enable_client_company);
	Optional<EnableToComment> findByUserIdAndRateeCompanyIdAndEnableClientCompany(Long userId, Long companyId, boolean b);
	Boolean existsByUserIdAndRateeCompanyIdAndEnableClientArticle(Long myUserId, Long companyId, boolean b);
	Boolean existsByRaterCompanyIdAndRateeCompanyIdAndEnableClientArticle(Long myCompanyId, Long companyId, boolean b);
	EnableToComment findByRaterCompanyIdAndRateeCompanyIdAndEnableClientArticle(Long myCompanyId, Long companyId,boolean b);
	EnableToComment findByUserIdAndRateeCompanyIdAndEnableClientArticle(Long myUserId, Long companyId, boolean b);
	
	
	EnableToComment findByRaterCompanyIdAndRateeCompanyId(Long id, Long id2);
	EnableToComment findByUserIdAndRateeCompanyId(Long id, Long id2);

	
}
