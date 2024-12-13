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

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Services.ClientService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/client/")
@RequiredArgsConstructor
public class ClientController {

	private final ClientService clientService;
	
	private final CompanyService companyService;
	
	private final UserService userService;

	private final JwtAuthenticationFilter authenticationFilter;

	private final Logger logger = LoggerFactory.getLogger(ClientController.class);
	
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@DeleteMapping("{id}")
	public ResponseEntity<String> deleteById(@PathVariable Long id) {
		Company company = companyService.getCompany();
		return clientService.deleteClientByIdAndCompanyId(id, company);
	}
	
	@GetMapping("get_all_my/{id}")
	public List<ClientProviderRelationDto> getAllMyClient(@PathVariable Long id, @RequestParam int page , @RequestParam int pageSize){
		Company company = companyService.getCompany();
		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			company = companyService.getById(id).getBody();
		}
		return clientService.getAllMyClient(company, page, pageSize);
	}

	@PostMapping("add")
	public ResponseEntity<ClientProviderRelationDto> insertClient(@RequestParam("company") String companyDto,
			@RequestParam(value ="file", required = false) MultipartFile file
			) throws JsonMappingException, JsonProcessingException{
		Company company = companyService.getCompany();
		return clientService.insertClient(companyDto,file, company);
	}
	

	@PostMapping("add_without_image")
	public ResponseEntity<ClientProviderRelationDto> insertClientWithoutImage(@RequestParam("company") String companyDto) throws JsonMappingException, JsonProcessingException{
		Company company = companyService.getCompany();
		return clientService.insertClient(companyDto,null, company);
	}
	
	@PutMapping("update")
	public ResponseEntity<CompanyDto> updateClient(@RequestParam("company") String companyDto,
			@RequestParam(value ="file", required = false) MultipartFile file
			) throws JsonMappingException, JsonProcessingException{
		logger.warn("dhrab fi update");
		Company company = companyService.getCompany();
		return clientService.updateClient( companyDto,file, company);
	}
	
	@PutMapping("update_without_image")
	public ResponseEntity<CompanyDto> updateClientt(@RequestParam("company") String companyDto
			) throws JsonMappingException, JsonProcessingException {
		logger.warn("dhrab fi updatee");
		Company company = companyService.getCompany();
		return clientService.updateClientt( companyDto, company);
	}
		
	
	@GetMapping("get_all_my_client_containing/{id}")
	public List<ClientProviderRelationDto> getAllMyCointaining( @PathVariable Long id,@RequestParam SearchType searchType,@RequestParam String search, @RequestParam int page , @RequestParam int pageSize){
			logger.warn("atsdrgh");
		Company company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			return clientService.getAllMyContaining(search,id, page, pageSize);
		}
		return null;
	}



	@GetMapping("get_all_client_person_containing/{id}")
	public List<UserDto> getAllClientsPersonContaining( @PathVariable Long id,@RequestParam SearchType searchType,@RequestParam String search, @RequestParam int page , @RequestParam int pageSize){
		if(authenticationFilter.accountType == AccountType.COMPANY) {
		Company company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			if(searchType == SearchType.CLIENT) {
				return clientService.getAllMyPersonContaining(search, id, page, pageSize);
			}
		}
		}
			return clientService.getAllPersonContaining(search, page, pageSize);
	}

}
