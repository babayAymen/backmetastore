package com.example.meta.store.werehouse.Controllers;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.InventoryDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.InventoryService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/inventory")
@RequiredArgsConstructor
public class InventoryController {

	
	private final InventoryService inventoryService;
	
	private final CompanyService companyService;
	
	private final WorkerService workerService;
	
	private final UserService userService;
	
	private final JwtAuthenticationFilter authenticationFilter;
	

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@GetMapping("/getbycompany/{id}")
	public List<InventoryDto> getInventoryByCompany(@PathVariable Long id, @RequestParam int page, @RequestParam int pageSize){
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = new Company();
			if(user.getRole() == RoleEnum.WORKER) {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}else {
			company = companyService.getCompany();
			}
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			return inventoryService.getInventoryByCompanyId(company.getId(), page , pageSize);
		}
		}
		return null;
	}

	
}
