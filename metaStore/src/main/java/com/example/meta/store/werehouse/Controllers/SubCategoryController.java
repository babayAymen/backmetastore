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
import com.example.meta.store.werehouse.Dtos.SubCategoryDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.SubCategory;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.SubCategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/subcategory/")
@RequiredArgsConstructor
public class SubCategoryController {

	
	private final SubCategoryService subCategoryService;
	
	private final CompanyService companyService;

	private final JwtAuthenticationFilter authenticationFilter;
	
	private final Logger logger = LoggerFactory.getLogger(SubCategoryController.class);
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	
	@GetMapping("getbycompany/{id}")
	public ResponseEntity<List<SubCategoryDto>> getSubCategoryByCompany(@PathVariable Long id){
//		User user = userService.getUser();
//		if(user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleEnum.ADMIN))) {
//			Company company = companyService.getCompany().orElseThrow(() -> new RecordNotFoundException("you don't have a company"));
//		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
//		}
//		}
		Company company = companyService.getCompany();
		return subCategoryService.getSubCategoryByCompany(company);
		
	}
	
	@PutMapping("update")
	public ResponseEntity<SubCategoryDto> upDateSubCategory(
			@RequestParam("sousCategory") String sousCategoryDto,
			@RequestParam(value="file",required=false) MultipartFile file) throws JsonMappingException, JsonProcessingException{
		Company company = companyService.getCompany();
		return subCategoryService.upDateSubCategory(sousCategoryDto,company,file);
	}
	
	@PostMapping("add")
	public ResponseEntity<SubCategoryDto> insertSubCategory(@RequestParam("sousCategory") String sousCategoryDto,
			@RequestParam(value = "file",required=false) MultipartFile file) throws JsonMappingException, JsonProcessingException{
		Company company = companyService.getCompany();
		return subCategoryService.insertSubCategory(sousCategoryDto,company,file);
	}
	
	@GetMapping("l/{name}")
	public ResponseEntity<SubCategoryDto> getSubCategoryById(@PathVariable String name){
		if(authenticationFilter.accountType == AccountType.COMPANY) {			
			Company company = companyService.getCompany();
		return subCategoryService.getSubCategoryById(name,company);
		}
		return subCategoryService.getSubCategoryById(name,new Company());
		
	}
	
	@DeleteMapping("delete/{id}")
	public void deleteSubCategoryById(@PathVariable Long id){
		Company  company = companyService.getCompany();
		subCategoryService.deleteSubCategoryById(id,company);
	}
	
	@GetMapping("getbycategory_id/{companyId}")
	public List<SubCategoryDto> getAllSubCategoriesByCompanyIdAndCategoryId(@PathVariable Long companyId,@RequestParam Long categoryId,@RequestParam int page , @RequestParam int pageSize ){
		Company company = companyService.getCompany();
		if(company.getId() == companyId || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(categoryId))) {			
			return subCategoryService.getAllSubCategoryByCompanyIdAndCategoryId(categoryId, companyId, page , pageSize);
		}
		return null;
	}

	
}
