package com.example.meta.store.werehouse.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.BeanDefinitionDsl.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.InvoiceDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.ClientService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.InvoiceService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/invoice/")
@RequiredArgsConstructor
public class InvoiceController {

	private final InvoiceService invoiceService;

	private final JwtAuthenticationFilter authenticationFilter;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final WorkerService workerService;

	private final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@GetMapping("getlastinvoice")
	public Long getLastInvoiceCode() {
		Company company = companyService.getCompany();
		return invoiceService.getLastInvoice(company.getId());
	}
	
	@GetMapping("getMyInvoiceAsProvider/{id}")
	public List<InvoiceDto> getMyInvoiceAsProvider(@PathVariable Long id){

		logger.warn("id is: "+id);
		Company company = null;
		List<InvoiceDto> invoiceList = new ArrayList<>();
		switch (authenticationFilter.accountType) {
		case COMPANY: {
			 company = companyService.getCompany();
				if(company.getId() == id ||  company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
					logger.warn(id+" company id from getMyInvoiceAsProvider controller");
					invoiceList = invoiceService.getMyInvoiceAsProvider(id, null);
				}
			break;
		}
		case USER: {
			break;
		}
		case WORKER :{
			company = getHisCompany().get();
			User user = userService.getUser();
			invoiceList = invoiceService.getMyInvoiceAsProvider(company.getId(),user.getId());
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + authenticationFilter.accountType);
		}

		logger.warn(invoiceList.size()+" size from getMyInvoiceAsProvider controller");
		return invoiceList;
		
	}
	
	@GetMapping("get_all_my_invoices_not_accepted")
	public List<InvoiceDto> getAllMyInvoicesNotAccepted() {
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			invoicesDto = invoiceService.getAllMyInvoicesNotAccepted(user.getId(),null);
		}
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			invoicesDto = invoiceService.getAllMyInvoicesNotAccepted(null,company.getId());
		}
		return invoicesDto;
	}
	
	@GetMapping("getMyInvoiceAsClient/{id}")
	public List<InvoiceDto> getInvoicesAsClient(@PathVariable Long id){
		logger.warn("id is: "+id);
		AccountType type = authenticationFilter.accountType;
		if(type == AccountType.COMPANY) {		
			Company company= new Company();
			company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			logger.warn("inside if");
			return invoiceService.getInvoicesAsClient(id,type);			
		}else {
			throw new NotPermissonException("you don't have permission for that");
		}
		
		}
		User user = userService.getUser();
		return invoiceService.getInvoicesAsClient(user.getId(), type);
	}
	
//	@GetMapping("getnotaccepted")
//	public List<InvoiceDto> getInvoiceNotifications(){
//		Company client = getCompany();
//		return invoiceService.getInvoiceNotifications(client,getUser().getId());
//	}
	
	@GetMapping("cancel_invoice/{id}")
	public void cancelInvoice(@PathVariable Long id) {
		Company company = companyService.getCompany();
		invoiceService.cancelInvoice(company, id);
	}
	
	
	private Optional<Company> getHisCompany() {
		Long companyId = workerService.getCompanyIdByUserName(authenticationFilter.userName);
		if(companyId != null) {			
		ResponseEntity<Company> company2 = companyService.getById(companyId);
		return Optional.of(company2.getBody());
		}
		return null;
	}
	

	
	
	

	@GetMapping("response/{type}/{invoice}")
	public void statusInvoice(@PathVariable String status, @PathVariable Long invoice) {
		Long clientId = 0L ;
		AccountType type = authenticationFilter.accountType;
		if(type == AccountType.USER) {
			clientId = userService.getUser().getId();
		}
		if(type == AccountType.COMPANY) {
		 clientId = companyService.getCompany().getId();
		}
		switch (status) {
		case "ACCEPT": {
			invoiceService.accepted(invoice,clientId, type);
			break;
		}
		case "REFUSE":{
			invoiceService.refused(invoice,clientId);
			break;
		}
		
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
	}
	
	
}
