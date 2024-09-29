package com.example.meta.store.werehouse.Controllers;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.werehouse.Dtos.InventoryDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.InventoryService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/inventory")
@RequiredArgsConstructor
public class InventoryController {

	
	private final InventoryService inventoryService;
	
	private final CompanyService companyService;
	

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@GetMapping("/getbycompany/{id}")
	public List<InventoryDto> getInventoryByCompany(@PathVariable Long id){
		Company company = companyService.getCompany();
		if(company.getId() != id && company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			company = companyService.getById(id).getBody();
		}
		return inventoryService.getInventoryByCompanyId(company.getId());
	}

	
}
