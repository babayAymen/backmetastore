package com.example.meta.store.werehouse.Controllers;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.LikeService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/like/")
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;
	private final UserService userService;
	private final CompanyService companyService;
	private final JwtAuthenticationFilter authenticationFilter;
	
	@GetMapping("{articleId}/{isFav}")
	public void LikeAnArticle(@PathVariable Long articleId, @PathVariable Boolean isFav) {
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.USER ) {
			 likeService.LikeAnArticle(articleId, null,user,isFav);
			 return;
		}else {
			Company company = companyService.getCompany();
			likeService.LikeAnArticle(articleId,company,null, isFav);
		}
	}
	

}
