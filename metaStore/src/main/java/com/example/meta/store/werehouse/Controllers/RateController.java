package com.example.meta.store.werehouse.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.RatersDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.RateType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.RateService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/rate/")
@RequiredArgsConstructor
public class RateController {

	private final RateService rateService;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final JwtAuthenticationFilter authenticationFilter;

	private final Logger logger = LoggerFactory.getLogger(RateController.class);
	@PostMapping("do_rate")
	public void rate(@RequestParam ("ratingDto") String rating, 
			 @RequestParam(value ="image", required = false) MultipartFile image) 
	throws Exception{
			logger.warn(" comment and type "+ rating);
			if(authenticationFilter.accountType == AccountType.USER) {			
			User user = userService.getUser();
			rateService.rate(rating,user, null,image);			
			}
			if(authenticationFilter.accountType == AccountType.COMPANY) {			
			Company myCompany = companyService.getCompany();
			rateService.rate(rating,null, myCompany,image);	
			}
		}
	
	@GetMapping("enable_to_comment_company/{companyId}")
	public Boolean enableToCommetCompany(@PathVariable Long companyId) {
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			return rateService.enableToCommentCompany(company.getId(),null,  companyId);
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			return rateService.enableToCommentCompany(null,user.getId(), companyId);
		}
		return null;
	}
	
	@GetMapping("enable_to_comment_user/{userId}")
	public Boolean enableToCommetUser(@PathVariable Long userId) {
			Company company = companyService.getCompany();
			return rateService.enableToCommentUser(company.getId(), userId);
	}
	
	@GetMapping("enable_to_comment_article/{companyId}")
	public Boolean enableToCommentArticle(@PathVariable Long companyId) {
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			return rateService.enableToCommentArticle(companyId, null, company.getId());
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			return rateService.enableToCommentArticle(companyId, user.getId(),null);
		}
	return null;	
	}
	
	
	@GetMapping("get_rate/{id}/{type}")
	public List<RatersDto> getAllRates(@PathVariable Long id, @PathVariable AccountType type){
		return rateService.getAllById(id,type);
	}
}
