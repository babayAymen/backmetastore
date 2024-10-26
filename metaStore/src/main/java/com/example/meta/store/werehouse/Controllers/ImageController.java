package com.example.meta.store.werehouse.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/werehouse/image/")
@RequiredArgsConstructor
public class ImageController {
	
	
		private final ImageService imageService;

		private final UserService userService;

		private final CompanyService companyService;
		
		private final JwtAuthenticationFilter authenticationFilter;
		private final Logger logger = LoggerFactory.getLogger(MessageController.class);

		
	@GetMapping(path = "{lien}/{service}/{id}")
	public byte[] getImage( @PathVariable String lien, @PathVariable String service, @PathVariable Long id)throws Exception {
		logger.warn(lien+" "+service+" "+id);
		return imageService.getImage( lien,service,id);
				}
	
	
	@PutMapping("update")
	public void updateImage(
			@RequestParam(value ="file", required = false) MultipartFile image) {
		logger.warn("update image");
		User user = userService.getUser();
		Company company = null;
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			 company = companyService.getCompany();
		}
		imageService.updateImage(authenticationFilter.accountType,image,user, company);
	}
}
