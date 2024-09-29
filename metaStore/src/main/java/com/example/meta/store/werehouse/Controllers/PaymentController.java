package com.example.meta.store.werehouse.Controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.BankTransferDto;
import com.example.meta.store.werehouse.Dtos.BillDto;
import com.example.meta.store.werehouse.Dtos.CashDto;
import com.example.meta.store.werehouse.Dtos.CheckDto;
import com.example.meta.store.werehouse.Dtos.PaymentDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Services.BankTransferService;
import com.example.meta.store.werehouse.Services.BillService;
import com.example.meta.store.werehouse.Services.CashService;
import com.example.meta.store.werehouse.Services.CheckService;
import com.example.meta.store.werehouse.Services.ClientService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.PaymentService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/werehouse/payment/")
@RequiredArgsConstructor
public class PaymentController {

	private final CashService cashService;
	
	private final CheckService checkService;
	
	private final BillService billService;
	
	private final BankTransferService bankTransferService;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final PaymentService paymentService;
	

	private final Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	@PostMapping("cash")
	public void invoiceCashPayment(@RequestBody CashDto cashDto) {
		Company client = companyService.getCompany();
		cashService.invoiceCashPayment(client, cashDto);
	}
	
	@PostMapping("check")
	public void invoiceCheckPayment(@RequestBody CheckDto checkDto) {
		Company client = companyService.getCompany();
		checkService.invoiceCheckPayment(client, checkDto);
	}
	
	@PostMapping("bill")
	public void invoiceBillPayment(@RequestBody BillDto billDto) {
		Company client = companyService.getCompany();
		billService.invoiceBillPayment(client, billDto);
	}
	
	@PostMapping("bank")
	public void invoiceBankTransferPayment(@RequestBody BankTransferDto bankTransferDto) {
		Company client = companyService.getCompany();
		bankTransferService.invoiceBankTransferPayment(client, bankTransferDto);
	}
	
	@GetMapping("get_all_my")
	public List<PaymentDto> getAllMy(){
		Company client = companyService.getCompany();
		
		return paymentService.getAllMy(userService.getUser().getId(), client.getId());
	}
	
	@GetMapping("get_all_my_as_company")
	public List<PaymentDto> getAllMyAsCompany(){
		Company company = companyService.getCompany();
		return paymentService.getAllMy(null,company.getId());
	}
	
	@GetMapping("get_all_my_as_client")
	public List<PaymentDto> getAllMyAsClient(){
		return paymentService.getAllMy(userService.getUser().getId(), null);
		
	}

	@GetMapping("{id}")
	public PaymentDto getMyById(@PathVariable Long id ) {
		return paymentService.getMyById(id);
	}
	
	@GetMapping("{response}/{id}")
	public void paymentResponse(@PathVariable Status response, @PathVariable Long id) {
		Company company = companyService.getCompany();
		paymentService.paymentResponse(response, id, company);
	}
	

}
