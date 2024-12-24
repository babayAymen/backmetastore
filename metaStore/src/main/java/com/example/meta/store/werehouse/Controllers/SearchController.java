package com.example.meta.store.werehouse.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.BeanDefinitionDsl.Role;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Dtos.SearchHistoryDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.SearchService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/search/")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final WorkerService workerService;

	private final JwtAuthenticationFilter authenticationFilter;
	
	private final Logger logger = LoggerFactory.getLogger(SearchController.class);
//	

	
	@GetMapping("user/{search}/{type}/{category}")
	public List<UserDto> getAllUserContaining(@PathVariable String search, @PathVariable SearchType type, @PathVariable SearchCategory category){
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = new Company();
			if(user.getRole() == RoleEnum.WORKER) {
					company = workerService.findCompanyByWorkerId(user.getId()).get();
			}else {
					company = companyService.getCompany();
			}
		return searchService.getAllUserContaining(search, type,category,company,user);
		}

		return searchService.getAllUserContaining(search, type,category,null,user);
	}
	
	@GetMapping("save_history/{category}/{id}")
	public SearchHistoryDto saveHistory(@PathVariable SearchCategory category, @PathVariable Long id) {
		AccountType type = authenticationFilter.accountType;
		User user = userService.getUser();
		if(type == AccountType.USER) {			
		 return searchService.saveHistory(category,id,user, null);
		}
		if(type == AccountType.COMPANY) {
			Company company = new Company();
			if(user.getRole() == RoleEnum.WORKER) {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}else {
				company = companyService.getCompany();
			}
			return searchService.saveHistory(category,id,null, company);

		}
		else return null;
	}
	
	@DeleteMapping("delete_history/{id}")
	public void deleteSearch(@PathVariable Long id) {
		searchService.deleteSearch(id);
	}
	
	@GetMapping("get_search_history/{id}")
	public Page<SearchHistoryDto> getSearchHistory(@PathVariable Long id , @RequestParam int page , @RequestParam int pageSize){
		AccountType type = authenticationFilter.accountType;
		User user = userService.getUser();
		logger.warn("type"+type);
		if(type == AccountType.USER || type == AccountType.META) {			
		return searchService.getSearchHistory(user.getId(), user, type, page , pageSize);
		}
		if(type == AccountType.COMPANY) {
			Company company = new Company();
			if(user.getRole() == RoleEnum.ADMIN || user.getRole() == RoleEnum.PARENT) {
			company = companyService.getCompany();
			}else {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}
			if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {				
			return searchService.getSearchHistory(id,user, type, page , pageSize );
			}
		}
		return null;
	}

}
