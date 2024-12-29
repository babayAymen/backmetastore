package com.example.meta.store.werehouse.Controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.InvetationDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Worker;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Enums.Type;
import com.example.meta.store.werehouse.Services.ClientService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.InvetationService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/invitation/")
@RequiredArgsConstructor
public class InvetationController {

	private final InvetationService invetationService;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final JwtAuthenticationFilter authenticationFilter;
	
	private final WorkerService workerService;
	
	private final Logger logger = LoggerFactory.getLogger(InvetationController.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@GetMapping("response/{status}/{id}")
	public void requestResponse(@PathVariable Long id, @PathVariable Status status) {
		invetationService.requestResponse(id,status);
	} 

	@GetMapping("get_invetation/{companyId}")
	public Page<InvetationDto> getInvetation(@PathVariable Long companyId , @RequestParam int page , @RequestParam int pageSize){
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {	
			Company company = new Company();
			if(user.getRole() == RoleEnum.WORKER) {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}else {
				company = companyService.getCompany();
			}

			if(company.getId() == companyId || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(companyId))) {	
			return invetationService.getInvetation(companyId, page, pageSize);
			}
		}
		return invetationService.getInvitationAsUser(user.getId(), page , pageSize);
	}
 
	
	@GetMapping("cancel/{id}")
	public void cancelRequestOrDeleteFriend(@PathVariable Long id) {
		User user = userService.getUser();
		Company provider = null;
		if(authenticationFilter.accountType == AccountType.COMPANY) {			
			provider = companyService.getCompany();
		}
		invetationService.cancelRequestOrDeleteFriend(user.getId(), provider, id);
	}
	
//	@GetMapping("parent/{id}")
//	public void sendParentInvetation(@PathVariable Long id ) {
//		Optional<Company> company = getCompany();
//		Company reciver  = companyService.getById(id).getBody();
//		invetationService.sendParentInvetation(company.get(), reciver);
//	}

	@GetMapping("send/{id}/{type}")
	public void addExistClient(@PathVariable Long id, @PathVariable Type type) {
		User user = userService.getUser();
		Company company = null;
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			company = companyService.getCompany();
		}
		invetationService.addInvitation(id,type,company,user);
	}
	

}
