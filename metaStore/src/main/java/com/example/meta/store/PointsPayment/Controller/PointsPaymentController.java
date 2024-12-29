package com.example.meta.store.PointsPayment.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.PointsPayment.Dto.PaymentForProviderPerDayDto;
import com.example.meta.store.PointsPayment.Dto.PaymentForProvidersDto;
import com.example.meta.store.PointsPayment.Dto.PointsPaymentDto;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviders;
import com.example.meta.store.PointsPayment.Entity.PointsPayment;
import com.example.meta.store.PointsPayment.Mapper.PointsPaymentMapper;
import com.example.meta.store.PointsPayment.Service.PaymentForProvidersSevice;
import com.example.meta.store.PointsPayment.Service.PointsPaymentService;
import com.example.meta.store.aymen.dto.ReglementForProviderDto;
import com.example.meta.store.werehouse.Dtos.PaymentDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/point/")
@RequiredArgsConstructor
public class PointsPaymentController {


	private final JwtAuthenticationFilter authenticationFilter;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final PointsPaymentService pointsPaymentService;
	
	private final PaymentForProvidersSevice paymentForProvidersSevice;

	private final Logger logger = LoggerFactory.getLogger(PointsPaymentController.class);
	@PostMapping()
	public void sendPoints(@RequestBody PointsPaymentDto pointPaymentDto) {
        logger.info("Received PointsPaymentDto amount: {}", pointPaymentDto.getAmount());
		Company company = companyService.getCompany();
		pointsPaymentService.sendPoints(pointPaymentDto,company);
		
	}
	
	@PostMapping("send_reglement")
	public void sendReglement(@RequestBody ReglementForProviderDto reglement) {
		pointsPaymentService.sendReglement(reglement);
	}
	
	
	@GetMapping("get_all_my/{companyId}")
	public List<PointsPaymentDto> getAllMyPointsPayment(@PathVariable Long companyId, @RequestParam int page , @RequestParam int pageSize){
		AccountType type  = authenticationFilter.accountType;
		List<PointsPaymentDto> points = new ArrayList<>();
		User user = userService.getUser();
		if(type == AccountType.USER) {			
			points = pointsPaymentService.getAllMyPointsPayment(0L, user.getId(), page, pageSize);
		}
		if(type == AccountType.COMPANY) {
		Company company = companyService.getCompany();
		if(company.getId() == companyId || company.getBranches().stream().anyMatch(compani ->compani.getId().equals(companyId) )) {
			points = pointsPaymentService.getAllMyPointsPayment(companyId, user.getId(), page , pageSize);
		}
		}
		return points;
		
	}
	
	@ResponseBody
	@GetMapping("get_all_my_payment/{companyId}")
	public Page<PaymentForProvidersDto> getAllMyPayments(@PathVariable Long companyId, @RequestParam int page , @RequestParam int pageSize) {
		logger.warn("dhrab id  : "+companyId);
		Company company = companyService.getCompany();
		if(company.getId() == companyId || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(companyId))) {
		return paymentForProvidersSevice.getAllMyPayments(companyId, page, pageSize);
		}
		return null;
	}

	
	@GetMapping("get_all_my_as_company/{id}")
	public List<PaymentForProvidersDto> getAllMyAsCompanyByDate(@PathVariable Long id,@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate findate, @RequestParam int page , @RequestParam int pageSize ){
		Company company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
		return  paymentForProvidersSevice.getAllMyAsCompanyByDate(date, findate,id , page , pageSize);
		}
			return null;
	}
	
	@GetMapping("get_my_profit_by_date/{date}/{findate}")
	public String getMyProfitByDate(@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate findate) {
		Company company = companyService.getCompany();
		return paymentForProvidersSevice.getSumAmountByDate(date,findate,company.getId());
	}
	
	@GetMapping("get_all_my_profits/{id}")
	public List<PaymentForProviderPerDayDto> getAllMyProfits(@PathVariable Long id , @RequestParam int page , @RequestParam int pageSize){
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			
		Company company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
		return paymentForProvidersSevice.getAllMyProfits(id, page , pageSize);
		}
		}
		else {
			User user  = userService.getUser();
			if(user.getId() == 1L) {
				logger.warn("id is : "+ id);
				return paymentForProvidersSevice.getAllMyProfits(id, page , pageSize);

			}
		}
		return null;
	}

	@GetMapping("get_all_my_profits_per_day/{id}")
	public List<PaymentForProviderPerDayDto> getAllMyProfitsPerDay(@PathVariable Long id ,@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beginDay,
			@RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finalDay,@RequestParam int page , @RequestParam int pageSize ){
		Company company = companyService.getCompany();
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
			return paymentForProvidersSevice.getAllMyProfitsPerDay(beginDay, finalDay, id, page , pageSize);			
		}
		return null;
	}
	
	@GetMapping("get_all_my_reglement_by_payment_id/{paymentId}")
	public Page<ReglementForProviderDto> getAllReglementByPaymentId(@PathVariable Long paymentId, @RequestParam int page , @RequestParam int pageSize ){
		if(authenticationFilter.accountType == AccountType.META) {
			return paymentForProvidersSevice.getAllReglementByPaymentId(paymentId , page , pageSize);
		}
		return null;
	}


}









