package com.example.meta.store.werehouse.Services;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.CompanyController;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Like;
import com.example.meta.store.werehouse.Mappers.LikeMapper;
import com.example.meta.store.werehouse.Repositories.LikeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService extends BaseService<Like, Long> {

	private final LikeRepository likeRepository;
	
	private final LikeMapper likeMapper;
	
	private final ArticleService articleService;
	
	private final Logger logger = LoggerFactory.getLogger(LikeService.class);
	
	public void LikeAnArticle(Long articleId, Company ccompany, User user, Boolean isFav) {
		ArticleCompany article = articleService.findByArticleCompanyId(articleId);
		if(isFav) {
		article.setLikeNumber(article.getLikeNumber()+1);
		if(user == null) {			
		Set<Company> companies = new HashSet<>();
		companies.add(ccompany);
		companies.addAll(article.getCompanies());
		article.setCompanies(companies);
		}else {
		Set<User> users = new HashSet<>();
		users.add(user);
		users.addAll(article.getUsers());
		article.setUsers(users);
		}
		}else {
			article.setLikeNumber(article.getLikeNumber()-1);
			logger.warn("like number : "+article.getLikeNumber());
			if(user == null) {			
				Set<Company> companies = article.getCompanies();
				companies.remove(ccompany);
				article.setCompanies(companies);
				}else {
				Set<User> users = article.getUsers();
				users.remove(user);
				article.setUsers(users);
				}
		}
	
	}
}
