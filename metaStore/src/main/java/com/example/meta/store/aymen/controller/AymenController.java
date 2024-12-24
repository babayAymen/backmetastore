package com.example.meta.store.aymen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.aymen.service.AymenService;
import com.example.meta.store.werehouse.Enums.AccountType;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/meta/")
@RequiredArgsConstructor
public class AymenController {

	private final UserService userService;
	
	private final AymenService aymenService;

	private final Logger logger = LoggerFactory.getLogger(AymenController.class);
	@GetMapping("make_as_point_seller/{status}/{companyId}")
	public void makeCompanyAsPointSeller(@PathVariable Boolean status, @PathVariable Long companyId) {
		User user = userService.getUser();
		if(user.getAccountType() == AccountType.META) {
			aymenService.makeCompanyAsPointSeller(companyId, status);
		}
	}
}
