package com.example.meta.store.werehouse.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.BeanDefinitionDsl.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.Status;
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
	public List<InvoiceDto> getMyInvoiceAsProvider(@PathVariable Long id, @RequestParam int page , @RequestParam int pageSize){
		Company company = null;
		List<InvoiceDto> invoiceList = new ArrayList<>();
		switch (authenticationFilter.accountType) {
		case COMPANY: {
			 company = companyService.getCompany();
				if(company.getId() == id ||  company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
					invoiceList = invoiceService.getMyInvoiceAsProvider(id, null, page, pageSize);
				}
			break;
		}
		case USER: {
			break;
		}
		case WORKER :{
			company = getHisCompany().get();
			User user = userService.getUser();
			invoiceList = invoiceService.getMyInvoiceAsProvider(company.getId(),user.getId(), page , pageSize);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + authenticationFilter.accountType);
		}

		logger.warn(invoiceList.size()+" size from getMyInvoiceAsProvider controller"+id);
		return invoiceList;
		
	}
	

	@GetMapping("get_by_payment_paid_status/{companyId}")
	public List<InvoiceDto> getAllMyInvoiceAsProviderAndStatus(@PathVariable Long companyId , @RequestParam PaymentStatus status, @RequestParam int page , @RequestParam int pageSize){
		Company company = companyService.getCompany();
		if(company.getId() == companyId || company.getBranches().stream().anyMatch(branch -> branch.getId().equals(companyId))) {			
			return invoiceService.getAllMyInvoicesAsProviderAndStatus(companyId , status, page , pageSize);
		}
		return null;
	}
	

	@GetMapping("get_by_payment_paid_status_as_client/{companyId}")
	public List<InvoiceDto> getAllBuyHistoryByPaidStatusAsClient(@PathVariable Long companyId , @RequestParam PaymentStatus status, @RequestParam int page , @RequestParam int pageSize){
		Company company = companyService.getCompany();
		if(company.getId() == companyId || company.getBranches().stream().anyMatch(branch -> branch.getId().equals(companyId))) {			
			return invoiceService.getAllMyInvoicesAsClientAndPaymentStatus(companyId , status, page , pageSize);
		}
		return null;
	}
	

	@GetMapping("get_all_my_invoices_not_accepted_as_client/{id}")
	public List<InvoiceDto> getAllMyInvoicesNotAcceptedAsClient(@PathVariable Long id , @RequestParam int page , @RequestParam int pageSize) {
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			invoicesDto = invoiceService.getAllMyInvoicesNotAcceptedAsClient(user.getId(),null, page , pageSize);
		}
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			invoicesDto = invoiceService.getAllMyInvoicesNotAcceptedAsClient(null,company.getId(), page , pageSize);
		}
		logger.warn("not accepted size :"+ invoicesDto.size());
		return invoicesDto;
	}
	
	@GetMapping("get_all_my_invoices_not_accepted_as_provider/{id}")
	public List<InvoiceDto> getAllMyInvoicesNotAcceptedAsProvider(@PathVariable Long id , @RequestParam Status status, @RequestParam int page , @RequestParam int pageSize){
		Company company  = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branch -> branch.getId().equals(id))) {
			return invoiceService.getAllMyInvoicesNotAcceptedAsProvider(id , status , page , pageSize);			
		}
		return null;
	}
	
	@GetMapping("getMyInvoiceAsClient/{id}")
	public List<InvoiceDto> getInvoicesAsClient(@PathVariable Long id, @RequestParam int page , @RequestParam int pageSize){
		logger.warn("id is: "+id);
		AccountType type = authenticationFilter.accountType;
		if(type == AccountType.COMPANY) {
			Company company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			return invoiceService.getInvoicesAsClient(id,type, page, pageSize);			
		}else {
			throw new NotPermissonException("you don't have permission for that");
		}
		
		}
		User user = userService.getUser();
		return invoiceService.getInvoicesAsClient(user.getId(), type, page, pageSize);
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
	

	@GetMapping("response/{invoice}/{status}")
	public void statusInvoice(@PathVariable Status status, @PathVariable Long invoice) {
		Long clientId = 0L ;
		AccountType type = authenticationFilter.accountType;
		if(type == AccountType.USER) {
			clientId = userService.getUser().getId();
		}
		if(type == AccountType.COMPANY) {
		 clientId = companyService.getCompany().getId();
		}
		switch (status) {
		case ACCEPTED: {
			invoiceService.accepted(invoice,clientId, type);
			break;
		}
		case REFUSED:{
			invoiceService.refused(invoice,clientId);
			break;
		}
		
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
	}

	@GetMapping("get_by_status_as_client/{id}")
	public List<InvoiceDto> getAllMyInvoicesAsClientAndStatus(@PathVariable Long id , @RequestParam Status status, @RequestParam int page , @RequestParam int pageSize){

		AccountType type = authenticationFilter.accountType;
		if(type == AccountType.USER) {
			User user = userService.getUser();
			if(user.getId() == id) {
			return invoiceService.getAllMyInvoicesAsClientAndStatus(id,status,type, page, pageSize);
			}else {
				throw new RecordNotFoundException("you dont have permission to do that");
			}
		}else {
			Company company = companyService.getCompany();
			if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			return invoiceService.getAllMyInvoicesAsClientAndStatus(id,status,type, page, pageSize);
			}else {
				throw new RecordNotFoundException("you dont have permission to do that");
			}
		}
	}
	
}





