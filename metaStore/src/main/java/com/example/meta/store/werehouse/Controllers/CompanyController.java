package com.example.meta.store.werehouse.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Services.CompanyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/werehouse/company/")
@RequiredArgsConstructor
public class CompanyController {

	private final CompanyService companyService;
	
	private final UserService userService;

	private final JwtAuthenticationFilter authenticationFilter;
	
	private final Logger logger = LoggerFactory.getLogger(CompanyController.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@PostMapping("add")
	public ResponseEntity<CompanyDto> insertCompany( 
			@RequestParam("company") String company,
			@RequestParam(value ="file", required = false) MultipartFile file)throws Exception{
		User user = userService.getUser();
		return companyService.insertCompany(company, file, user);
	}
	
	@PutMapping("update")
	public ResponseEntity<CompanyDto> upDateCompany(
			@RequestParam("company") String companyDto,
			@RequestParam(value ="file", required = false) MultipartFile file
			) throws Exception{
		return companyService.upDateCompany(companyDto, file);
	}
	

	
	@GetMapping("search/{branshe}")
	public List<CompanyDto> searchCompanyContaining(@PathVariable String branshe){
		Company company = companyService.getCompany();
		return companyService.getCompanyContaining(branshe, company.getId());
	}

	@GetMapping("{id}")
	public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id){
		return companyService.getCompanyById(id);		
	}
	
	@GetMapping("me")
	public CompanyDto getMe() {
		Company company = companyService.getCompany();
		return companyService.getMe(company,company.getId());
	}
	
	@GetMapping("mycompany/{id}")
	public CompanyDto getMe(@PathVariable Long id) {
		Company company = companyService.getCompany();
		if(!company.getId().equals(id) && id != 0) {
			boolean exists = company.getBranches().stream()
					.anyMatch(branch -> branch.getId().equals(id));
			if(!exists) {
				throw new RecordNotFoundException("you don't have a company");
			}
		}
		return companyService.getMe(company,id);
	}
	
//	@GetMapping("/rate/{id}/{rate}")
//	public void rateCompany(@PathVariable long id, @PathVariable double rate) {
//		companyService.rateCompany(id,rate);
//	}

	@GetMapping("all")
	public List<CompanyDto> getAll(){
		return companyService.getAllCompany();
	}
	
	@GetMapping("get_my_company_id")
	public Long getMyCompanyId() {
		Company company = companyService.getCompany();
		return company.getId();
	}
	
	@GetMapping("get_my_parent/{id}")
	public CompanyDto getMyParent(@PathVariable Long id) {
		Company company = companyService.getCompany();
		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			company = companyService.getById(id).getBody();
		}
		return companyService.getMyParent(company);
		}
	
	@GetMapping("get_branches")
	public List<CompanyDto> getBranches(){
		Company company = companyService.getCompany();
		return companyService.getBranches(company);
	}
	
	@GetMapping("get_companies_containing/{id}")
	public List<CompanyDto> getAllCompaniesContaining(@PathVariable Long id, @RequestParam String search , @RequestParam SearchType searchType, @RequestParam int page , @RequestParam int pageSize ){
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
				return companyService.getAllCompaniesContainig(null,company, search, page , pageSize, searchType);
			}
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			return companyService.getAllCompaniesContainig(user,null, search, page , pageSize, searchType);	
		}
		return null;
		
	}
	
	@GetMapping("update_location/{latitude}/{longitude}")
	public void updatelocation(@PathVariable Double latitude, @PathVariable Double longitude) {
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			companyService.updateLocation(latitude, longitude);
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			userService.updateLocation(latitude, longitude);
			
		}
		logger.warn(latitude +" "+longitude + "");
	}
	
	
}
