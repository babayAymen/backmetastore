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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.Type;
import com.example.meta.store.werehouse.Services.ClientService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/client/")
@RequiredArgsConstructor
public class ClientController {

	private final ClientService clientService;
	
	private final CompanyService companyService;
	

	private final Logger logger = LoggerFactory.getLogger(ClientController.class);
	
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@DeleteMapping("delete/{id}")
	public void deleteById(@PathVariable Long id) {
		Company company = companyService.getCompany();
		clientService.deleteClientByIdAndCompanyId(id, company);
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
	public void insertClient(@RequestParam("company") String companyDto,
			@RequestParam(value ="file", required = false) MultipartFile file
			) throws Exception{
		Company company = companyService.getCompany();
		clientService.insertClient(companyDto,file, company);
	}
	
	@PutMapping("/update")
	public void updateClient(@RequestParam("company") String companyDto,
			@RequestParam(value ="file", required = false) MultipartFile file
			) throws Exception{
		Company company = companyService.getCompany();
		clientService.updateClient( companyDto,file, company);
	}
		

	@GetMapping("get_all_my_containing/{value}/{id}")
	public List<ClientProviderRelationDto> getAllMyCointaining(@PathVariable String value, @PathVariable Long id){
		Company company = companyService.getCompany();
		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			company = companyService.getById(id).getBody();
		}
		return clientService.getAllMyContaining(value,company,SearchCategory.COMPANY);
	}


}
