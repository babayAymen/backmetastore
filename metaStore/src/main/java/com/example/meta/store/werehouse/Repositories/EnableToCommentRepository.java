package com.example.meta.store.werehouse.Repositories;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.EnableToComment;

public interface EnableToCommentRepository extends BaseRepository<EnableToComment, Long> {

	Boolean existsByRaterCompanyIdAndRateeCompanyIdAndEnableClientCompany(Long myCompanyId, Long companyId, boolean enable_client_company);
	Boolean existsByUserIdAndRateeCompanyIdAndEnableClientCompany(Long myCompanyId, Long companyId, boolean enable_client_company);
	Boolean existsByRaterCompanyIdOrRateeCompanyIdAndUserId(Long raterCompanyId,Long rateeCompanyId, Long userId);
	EnableToComment findByRaterCompanyIdAndRateeCompanyIdAndEnableClientCompany(Long myCompanyId, Long companyId,boolean b);
	EnableToComment findByUserIdAndRateeCompanyIdAndEnableClientCompany(Long userId, Long companyId, boolean b);
	Boolean existsByUserIdAndRateeCompanyIdAndEnableClientArticle(Long myUserId, Long companyId, boolean b);
	Boolean existsByRaterCompanyIdAndRateeCompanyIdAndEnableClientArticle(Long myCompanyId, Long companyId, boolean b);
	EnableToComment findByRaterCompanyIdAndRateeCompanyIdAndEnableClientArticle(Long myCompanyId, Long companyId,boolean b);
	EnableToComment findByUserIdAndRateeCompanyIdAndEnableClientArticle(Long myUserId, Long companyId, boolean b);
	EnableToComment findByRaterCompanyIdAndRateeCompanyId(Long id, Long id2);
	EnableToComment findByUserIdAndRateeCompanyId(Long id, Long id2);

	
}
