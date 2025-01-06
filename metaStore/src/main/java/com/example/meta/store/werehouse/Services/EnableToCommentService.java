package com.example.meta.store.werehouse.Services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.EnableToComment;
import com.example.meta.store.werehouse.Repositories.EnableToCommentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class EnableToCommentService extends BaseService<EnableToComment, Long>{

	private final EnableToCommentRepository enableToCommentRepository;

	public Boolean existByUserIdAndCompanyId(Long myCompanyId, Long userId, Long companyId) {
		Boolean exists = false;
		if(myCompanyId != null) {
			exists = enableToCommentRepository.existsByRaterCompanyIdAndRateeCompanyIdAndEnableClientCompany(myCompanyId, companyId, true);
			log.info("my company id : and userId : and companyId : and exists",myCompanyId, userId , companyId, exists);
		}
		if(userId != null) {
			exists = enableToCommentRepository.existsByUserIdAndRateeCompanyIdAndEnableClientCompany(userId, companyId, true);
			log.info("my company id : and userId : and companyId : and exists",myCompanyId, userId , companyId, exists);
		}
		log.info("my company id : and userId : and companyId : and exists",myCompanyId, userId , companyId, exists);
		return exists;
	}
	
	public Boolean existsByUserIdAndCompanyId(Long companyId , Long userId) {
		Boolean exists = enableToCommentRepository.existsByRaterCompanyIdOrRateeCompanyIdAndUserId(companyId,companyId, userId);
		log.info(" userId : and companyId : and exists", userId , companyId, exists);
		return exists;
	}

	public void makeEnableToComment(Company company, User person, Company client) {
		EnableToComment enableToComment = null;
		if(client != null) {
			enableToComment = enableToCommentRepository.findByRaterCompanyIdAndRateeCompanyId(client.getId(), company.getId());
		}
		if(person != null) {
			enableToComment = enableToCommentRepository.findByUserIdAndRateeCompanyId(person.getId(), company.getId());
		}
		if(enableToComment == null) {
			enableToComment = new EnableToComment();
			enableToComment.setRateeCompany(company);
			enableToComment.setRaterCompany(client);
			enableToComment.setUser(person);
		}
		enableToComment.setEnableClientArticle(true);
		enableToComment.setEnableClientCompany(true);
		enableToComment.setEnableProvider(true);
		enableToCommentRepository.save(enableToComment);
		
	}

	public void makeDisableToCommentCompany(Long companyId, Long myCompanyId, Long myUserId) {
		Optional<EnableToComment> enableToComment = Optional.empty();
		if(myCompanyId != null) {
			 enableToComment = enableToCommentRepository.findByRaterCompanyIdAndRateeCompanyIdAndEnableClientCompany(myCompanyId, companyId, true);
			
		}
		if(myUserId != null) {
			 enableToComment = enableToCommentRepository.findByUserIdAndRateeCompanyIdAndEnableClientCompany(myUserId, companyId, true);
		}
		if(enableToComment.isPresent()) {
			if(enableToComment.get().getRateeCompany().getId() == myCompanyId) {
				enableToComment.get().setEnableProvider(false);
			}else {
			enableToComment.get().setEnableClientCompany(false);
			} 
			enableToCommentRepository.save(enableToComment.get());
		}
	}

	public void makeDisableToCommentArticle(Long companyId, Long myCompanyId, Long myUserId) {
		EnableToComment enableToComment = new EnableToComment();
		if(myCompanyId != null) {
			 enableToComment = enableToCommentRepository.findByRaterCompanyIdAndRateeCompanyIdAndEnableClientArticle(myCompanyId, companyId, true);
			
		}
		if(myUserId != null) {
			 enableToComment = enableToCommentRepository.findByUserIdAndRateeCompanyIdAndEnableClientArticle(myUserId, companyId, true);
		}
		if(enableToComment.getId() != null) {
			enableToComment.setEnableClientArticle(false);
			enableToCommentRepository.save(enableToComment);
		}
	}

	public Boolean enableToCommentArticle(Long companyId, Long myUserId, Long myCompanyId) {
		Boolean exists = false;
		if(myUserId != null) {
			exists = enableToCommentRepository.existsByUserIdAndRateeCompanyIdAndEnableClientArticle(myUserId, companyId, true);
		}
		if(myCompanyId != null) {
			exists = enableToCommentRepository.existsByRaterCompanyIdAndRateeCompanyIdAndEnableClientArticle(myCompanyId, companyId, true);
		}
		return exists;
	}
	
}
