package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.SearchController;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyWithoutTroubleDto;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Dtos.SearchHistoryDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.SearchHistory;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Mappers.SearchHistoryMapper;
import com.example.meta.store.werehouse.Repositories.ClientProviderRelationRepository;
import com.example.meta.store.werehouse.Repositories.SearchHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService extends BaseService<SearchHistory, Long> {
	
	private final CompanyService companyService;
	
	private final ProviderService providerService;
	
	private final ClientService clientService;
	
	private final ArticleService articleService;

	private final UserService userService;
	
	private final ClientProviderRelationRepository clientProviderRelationRepository;
	
	private final SearchHistoryRepository searchHistoryRepository;
	
	private final SearchHistoryMapper searchHistoryMapper;

	private final Logger logger = LoggerFactory.getLogger(SearchService.class);
	
//	public List<CompanyDto> getAllContaining(String search, SearchType type, SearchCategory category, Company company, User user) {
//		switch (type) {
//		case PROVIDER: {
//			if(company != null) {		
//			return providerService.getAllProvidersContaining(company.getId(), search,0L);
//			}else {
//				return providerService.getAllProvidersContaining(0L,search, user.getId());
//			}
//		}
//
//		case OTHER : {
//			switch (category) {
//			case COMPANY: {
//				return companyService.getAllCompaniesContainig(user, company, search);
//			}
//
//			default:
//				throw new IllegalArgumentException("Unexpected value: " + category);
//			}
//		}
//		default:
//			throw new IllegalArgumentException("Unexpected value: " + type);
//		}
//	}
	
	public List<UserDto> getAllUserContaining(String search, SearchType type, SearchCategory category, Company company, User user) {
		switch (type) {
		
		case CLIENT : {			
		//	return clientService.getAllMyContaining(search, company,category);
		return	clientService.findAllMyByNameContainingOrCodeContainingAndPersonId(search, company.getId());
		}
		case OTHER : {
			switch (category) {
			
			case USER : {
				logger.warn(search+" get all user containing other case category tpe");
				return userService.getAllUsersContaining(search);
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + category);
			}
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}

	
	
	
	
	
	
	
	public SearchHistoryDto saveHistory(SearchCategory category, Long itemId, User meUser, Company meCompany) {
		SearchHistory history = new SearchHistory();
		Optional<SearchHistory> hist = null;
		switch (category) {
		case ARTICLE: {
			if(meUser != null) {				
			 hist = searchHistoryRepository.existsByArticleAndSearchCategoryAndSearcher(itemId,category,meUser.getId(),0L);
			}else {
				 hist = searchHistoryRepository.existsByArticleAndSearchCategoryAndSearcher(itemId,category,0L, meCompany.getId());

			}
			if(hist.isPresent()) {
				history = hist.get();
				break;
			}
			ArticleCompany article = articleService.findArticleCompanyById(itemId);
			history.setArticle(article);
			break;
		}
		case COMPANY: {
			if(meUser != null) {				
			 hist = searchHistoryRepository.existsByCompanyAndSearchCategoryAndSearcher(itemId,category,meUser.getId(),0L);
			}else {
				 hist = searchHistoryRepository.existsByCompanyAndSearchCategoryAndSearcher(itemId,category,0L,meCompany.getId());
			}
			if(hist.isPresent()) {
				history = hist.get();
				break;
			}
					Company company = companyService.getById(itemId).getBody();
					history.setCompany(company);
					break;
				}
		case USER: {
			if(meUser != null) {				
			 hist = searchHistoryRepository.existsByUserAndSearchCategoryAndSearcher(itemId,category,meUser.getId(),0L);
			}else {
				 hist = searchHistoryRepository.existsByUserAndSearchCategoryAndSearcher(itemId,category,0L,meCompany.getId());
			}
			if(hist.isPresent()) {
				history = hist.get();
				break;
			}
			User user = userService.findById(itemId).get();
			history.setUser(user);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + category);
		}
		if(meUser != null) {
			history.setMeUser(meUser);
		}else {
			history.setMeCompany(meCompany);
		}
		history.setSearchCategory(category);
		searchHistoryRepository.save(history);
		SearchHistoryDto response = searchHistoryMapper.mapToDto(history);
		logger.warn("c bon saved");
		return response;
		
		
	}
	
	public void deleteSearch(Long id) {
		searchHistoryRepository.deleteById(id);
	}
	
	public Page<SearchHistoryDto> getSearchHistory(Long id, User user , AccountType type, int page , int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC,"lastModifiedDate");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		switch (type) {
		case USER: {
			Page<SearchHistory> histories = searchHistoryRepository.findAllByMeUserId(id, pageable);
			List<SearchHistoryDto> dtos = histories.stream().map(searchHistoryMapper::mapToDto).toList();
			logger.warn(dtos.size()+" size");
			return new PageImpl<>(dtos, pageable, histories.getTotalElements()) ;
		}
		case META: {
			Page<SearchHistory> histories = searchHistoryRepository.findAllByMeUserId(id, pageable);
			List<SearchHistoryDto> dtos = histories.stream().map(searchHistoryMapper::mapToDto).toList();
			logger.warn(dtos.size()+" size");
			return new PageImpl<>(dtos, pageable, histories.getTotalElements()) ;
		}
		case COMPANY: {
			Page<SearchHistory> histories = Page.empty();
			if(user.getRole() == RoleEnum.WORKER) {
				logger.warn("company id : "+id + " and user id is : "+user.getId());
				histories = searchHistoryRepository.findAllByMeCompanyIdAndLastModifiedBy(id , user.getId() , pageable);
			}else {
			 histories = searchHistoryRepository.findAllByMeCompanyId(id, pageable);
			}
			List<SearchHistoryDto> dtos = histories.stream().map(searchHistoryMapper::mapToDto).toList();
			
			logger.warn(dtos.size()+" size");
			return new PageImpl<>(dtos, pageable, histories.getTotalElements());
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		
	}

	
	

}
