package com.example.meta.store.werehouse.Controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.PurchaseOrderDto;
import com.example.meta.store.werehouse.Dtos.PurchaseOrderLineDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.PurchaseOrderService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/werehouse/order/")
@RequiredArgsConstructor
public class PurchaseOrderController {

	private final PurchaseOrderService purchaseOrderService;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final WorkerService workerService;
	
	private final JwtAuthenticationFilter authenticationFilter;
	
	private final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@GetMapping("get_all_my_orders/{id}")
	public List<PurchaseOrderDto> getAllMyPerchaseOrdersNotAccepted(@PathVariable Long id){
		Company company = new Company();
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
		Company client = companyService.getCompany();
		if(client.getId() == id || client.getBranches().stream().anyMatch(compan -> compan.getId() == id)) {
			company = client;
		}
		}
		logger.warn(company.getId()+ "company id in get all my orders");
		return purchaseOrderService.getAllMyPerchaseOrdersNotAccepted(company, user.getId());
	}
	
	
	
	
	@PostMapping()
	public List<PurchaseOrderLineDto> addPurchaseOrder(@RequestBody List<PurchaseOrderLineDto> purchaseOrderDto) {
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company client = companyService.getCompany();
		return	purchaseOrderService.addPurchaseOrder(purchaseOrderDto,client,user);
		}
		return purchaseOrderService.addPurchaseOrder(purchaseOrderDto,null,user);
	}
	
	
	@GetMapping("get_all_my_lines/{companyId}")
	public List<PurchaseOrderLineDto> getrAllMyPurchaseOrderLines(@PathVariable Long companyId){
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			if(company.getId() != companyId &&  company.getBranches().stream().anyMatch(branche -> branche.getId().equals(companyId))) {
				return purchaseOrderService.getAllMyPurchaseOrderLinesByCompanyId(companyId,0L);
			}
			return purchaseOrderService.getAllMyPurchaseOrderLinesByCompanyId(company.getId(), 0L);
		}
		User user = userService.getUser();
		return purchaseOrderService.getAllMyPurchaseOrderLinesByCompanyId(0L,user.getId());
	}
	
	@GetMapping("get_lines/{id}")
	public List<PurchaseOrderLineDto> getAllPurchaseOrderLinesByPurchaseOrderId(@PathVariable Long id){
		return purchaseOrderService.getAllPurchaseOrderLinesByPurchaseOrderId(id);
	}
	
	@GetMapping("{id}")
	public PurchaseOrderDto getOrderById(@PathVariable Long id) {
		Company client = companyService.getCompany();
		User user = userService.getUser();
		return purchaseOrderService.getOrderById(id,client, user);
	}
	
	@GetMapping("{id}/{status}/{isall}")
	public Double OrderResponse(@PathVariable Long id, @PathVariable Status status, @PathVariable Boolean isall) {
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = new Company();
			if(user.getRole() == RoleEnum.WORKER) {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}else {				
				company = companyService.getCompany();
			}
			return purchaseOrderService.OrderResponse(id,status,company, isall);
		}
		return purchaseOrderService.OrderResponse(id,status,null, isall);
	}
	
	
	@PutMapping("")
	public void UpdatePurchaseOrderLine(@RequestBody PurchaseOrderLineDto purchaseOrderLineDto) {
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company client = companyService.getCompany();
			purchaseOrderService.UpdatePurchaseOrderLine(purchaseOrderLineDto,client.getId(),user.getId());			
		}
		purchaseOrderService.UpdatePurchaseOrderLine(purchaseOrderLineDto,null,user.getId());
	} 


	@GetMapping("get_all_my_orders_not_accepted/{id}")
	public List<PurchaseOrderLineDto> getAllMyOrdersNotAccepted(@PathVariable Long id , @RequestParam int page , @RequestParam int pageSize){
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = new Company();
			if(user.getRole() != RoleEnum.WORKER) {
			 company = companyService.getCompany();
			if(company.getId() != id &&  company.getBranches().stream().anyMatch(branche -> !branche.getId().equals(id))) {
				throw new RecordNotFoundException("you do not have permission to do that! ");
			}
			}else {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}
			
			return purchaseOrderService.getAllMyOrdersNotAcceptedAsProvider(id,page, pageSize);
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			return purchaseOrderService.getAllMyOrdersNotAcceptedAsClient(user.getId(), page,pageSize);
		}
		logger.warn("return null for getAllMyOrdersNotAccepted id: "+id+" andaccount type : "+authenticationFilter.accountType);
		return null;
	}
	
	@GetMapping("get_by_order_id/{id}")
	public List<PurchaseOrderLineDto> getAllPurchaseOrdersLineByOrderId(@PathVariable Long id, @RequestParam int page , @RequestParam int pageSize){
		return purchaseOrderService.getAllPurchaseOrdersLineByOrderId(id,page , pageSize);
	}
	
	@GetMapping("get_purchaseorder_line_by_invoice_id/{companyId}")
	public List<PurchaseOrderLineDto> getAllMyOrdersLinesByInvoiceId(@PathVariable Long companyId , @RequestParam Long invoiceId , @RequestParam int page , @RequestParam int pageSize){
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			if(company.getId() == companyId || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(companyId))) {
				return purchaseOrderService.getAllPurchaseOrderLinesByInvoice(invoiceId, company.getId(), null, page, pageSize);
			}
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			return purchaseOrderService.getAllPurchaseOrderLinesByInvoice(invoiceId, null, user.getId(), page , pageSize);
		}
		
		return null;
	}

	
}
