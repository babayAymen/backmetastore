package com.example.meta.store.aymen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.aymen.entity.ReglementForProvider;
import com.example.meta.store.aymen.service.AymenService;
import com.example.meta.store.werehouse.Enums.AccountType;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/meta/")
@RequiredArgsConstructor
public class MetaController {

	private final UserService userService;
	
	private final AymenService metaService;

	private final Logger logger = LoggerFactory.getLogger(MetaController.class);
	@GetMapping("make_as_point_seller/{companyId}")
	public void makeCompanyAsPointSeller(@RequestParam Boolean status, @PathVariable Long companyId) {
		User user = userService.getUser();
		if(user.getAccountType() == AccountType.META) {
			metaService.makeCompanyAsPointSeller(companyId, status);
		}
	}
	
	@GetMapping("make_as_meta_seller/{companyId}")
	public void makeCompanyAsMetaSeller(@RequestParam Boolean status, @PathVariable Long companyId) {
		User user = userService.getUser();
		if(user.getAccountType() == AccountType.META) {
			metaService.makeCompanyAsMetaSeller(companyId, status);
		}
	}
	
	@PostMapping("reglement_for_provider/{companyId}")
	public void reglemenyFoProvider(@PathVariable Long companyId , @RequestBody ReglementForProvider payment) {
		User user = userService.getUser();
		if(companyId == user.getId()) {
			metaService.reglemenyFoProvider(companyId , payment);
		}
	}
	
}
