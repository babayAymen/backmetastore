package com.example.meta.store.werehouse.Controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.ProviderService;
import com.example.meta.store.werehouse.Services.WorkerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/provider")
@RequiredArgsConstructor
public class ProviderController {

	private final ProviderService providerService;
	
	private final JwtAuthenticationFilter authenticationFilter;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final WorkerService workerService;

	private final Logger logger = LoggerFactory.getLogger(ProviderController.class);
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	

	@GetMapping("/get_all_my/{id}")
	public List<ClientProviderRelationDto> getAllMy(@PathVariable Long id){
		logger.warn("c bon je lil provider");
		Company company = companyService.getCompany();
		Long companyId = company.getId();
		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			companyId = id;
		}
		return providerService.getAllMyProvider(companyId);
	}
	
	
	
	@GetMapping("get_all_my_provider_containing/{search}/{id}")
	public List<CompanyDto> getAllProviderContaining(@PathVariable String search, @PathVariable Long id){
		Company company = companyService.getCompany();
		Long companyId = company.getId();
		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			companyId = id;
		}
		return providerService.getAllProvidersContaining(companyId, search,0L);
	}
	
	@GetMapping("/get_all_my_virtual")
	public List<ClientProviderRelationDto> getAllMyVirtual() {
		Company company = companyService.getCompany();
		return providerService.getAllMyVirtaul(company);
	}
	
	
	@PostMapping("/add")
	public void insertProvider(@RequestParam("company") String company,
	@RequestParam(value ="file", required = false) MultipartFile file)throws Exception{
		Company myCompany = companyService.getCompany();
		 providerService.insertProvider(company,file, myCompany);
	}
	
	@PutMapping("/update")
	public void upDateMyProviderById( @RequestParam("company") String company,@RequestParam(value ="file", required = false) MultipartFile file)
			throws JsonMappingException, JsonProcessingException {
		 providerService.updateProvider(company,file);
	}
	

	@DeleteMapping("/delete/{id}")
	public void deleteProvider(@PathVariable Long id) {
		Company company = companyService.getCompany();
		providerService.deleteProviderById(id,company);
	}
	
	@GetMapping("get_all_provider_containing/{search}")
	public List<CompanyDto> getAllProviderContaining(@PathVariable String search){
		logger.warn("get all provider contining ");
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {			
			Company company = companyService.getCompany();
			return providerService.getAllProviderContaining(search, company.getId(),null);
		}
		return providerService.getAllProviderContaining(search, null, user.getId());
		
	}

}
