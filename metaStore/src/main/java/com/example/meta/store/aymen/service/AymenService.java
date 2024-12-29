package com.example.meta.store.aymen.service;

import org.springframework.stereotype.Service;

import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.aymen.entity.ReglementForProvider;
import com.example.meta.store.werehouse.Services.CompanyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AymenService extends BaseService<ReglementForProvider, Long> {
	
	private final CompanyService companyService;
	
	public void makeCompanyAsPointSeller(Long companyId, Boolean status) {
		companyService.makeCompanyAsPointSeller(companyId, status);
		
	}

	public void makeCompanyAsMetaSeller(Long companyId , Boolean status) {
		companyService.makeCompanyAsMetaSeller(companyId , status);
	}

	public void reglemenyFoProvider(Long companyId, ReglementForProvider payment) {
		
		
	}
	
	
}
