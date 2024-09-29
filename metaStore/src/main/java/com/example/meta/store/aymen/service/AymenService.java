package com.example.meta.store.aymen.service;

import org.springframework.stereotype.Service;

import com.example.meta.store.werehouse.Services.CompanyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AymenService {
	
	private final CompanyService companyService;
	
	public void makeCompanyAsPointSeller(Long companyId, Boolean status) {
		companyService.makeCompanyAsPointSeller(companyId, status);
		
	}

	
	
}
