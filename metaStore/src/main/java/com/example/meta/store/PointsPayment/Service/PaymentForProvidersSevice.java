package com.example.meta.store.PointsPayment.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.PointsPayment.Dto.PaymentForProviderPerDayDto;
import com.example.meta.store.PointsPayment.Dto.PaymentForProvidersDto;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviderPerDay;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviders;
import com.example.meta.store.PointsPayment.Mapper.PaymentForProviderPerDayMapper;
import com.example.meta.store.PointsPayment.Mapper.PaymentForProvidersMapper;
import com.example.meta.store.PointsPayment.Repository.PaymentForProviderPerDayRepository;
import com.example.meta.store.PointsPayment.Repository.PaymentForProvidersRepository;
import com.example.meta.store.werehouse.Controllers.ArticleController;
import com.example.meta.store.werehouse.Dtos.PaymentDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Payment;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.CompanyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentForProvidersSevice extends BaseService<PaymentForProviders, Long>{

	
	private final PaymentForProvidersRepository paymentForProvidersRepository;

	private final PaymentForProvidersMapper paymentForProvidersMapper;
	
	private final JwtAuthenticationFilter authenticationFilter;
	
	private final CompanyService companyService;
	
	private final UserService userService;
	
	private final PaymentForProviderPerDayRepository paymentForProviderPerDayRepository;
	
	private final PaymentForProviderPerDayMapper paymentForProviderPerDayMapper;
	
	private final Logger logger = LoggerFactory.getLogger(PaymentForProvidersSevice.class);
	
	public void insertPaymentForProvider(PurchaseOrderLine purchaseOrder) {
		Double giveenespece = calculateGiveneEspece(purchaseOrder);
		PaymentForProviders paymentForProviders = new PaymentForProviders();
		paymentForProviders.setPurchaseOrderLine(purchaseOrder);
		paymentForProviders.setGiveenespece(giveenespece.doubleValue());
		paymentForProviders.setStatus(false);
		paymentForProvidersRepository.save(paymentForProviders);
		Optional<PaymentForProviderPerDay> perday =
				paymentForProviderPerDayRepository.findByProviderIdAndCreatedDate(purchaseOrder.getPurchaseorder().getCompany().getId(),LocalDate.now());
		if(perday.isEmpty()) {			
		PaymentForProviderPerDay paymentForProviderPerDay = new PaymentForProviderPerDay();
		paymentForProviderPerDay.setProvider(purchaseOrder.getPurchaseorder().getCompany());
		paymentForProviderPerDay.setPayed(false);
		paymentForProviderPerDay.setAmount(giveenespece);
		paymentForProviderPerDayRepository.save(paymentForProviderPerDay);
		}else {
			PaymentForProviderPerDay per = perday.get();
			Double sum = sumWithTwoValue(perday.get().getAmount(), giveenespece);
			per.setAmount(sum);
		}
		
	}
	
	
	public Double calculateGiveneEspece(PurchaseOrderLine purchaseOrder) {
	    BigDecimal sellingPrice = new BigDecimal(purchaseOrder.getArticle().getSellingPrice());
	    BigDecimal quantity = new BigDecimal(purchaseOrder.getQuantity());
	    BigDecimal giveneEspece = sellingPrice.multiply(new BigDecimal("0.9"))
	                                          .multiply(new BigDecimal("0.8"))
	                                          .multiply(quantity);

	    return giveneEspece.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	private Double sumWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.add(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	public List<PaymentForProvidersDto> getAllMyPayments(Long id , int page , int pageSize){
		Pageable pageable = PageRequest.of(page, pageSize);
			Page<PaymentForProviders>paymentForProviders = paymentForProvidersRepository.getPaymentForProvidersAsCompany(id, pageable);
		if(paymentForProviders.isEmpty()) {
			throw new RecordNotFoundException("there is no payment");
		}
		List<PaymentForProvidersDto> paymentForProvidersDto = new ArrayList<>();
		for(PaymentForProviders i : paymentForProviders) {
			PaymentForProvidersDto paymentForProviderDto = paymentForProvidersMapper.mapToDto(i);
			paymentForProvidersDto.add(paymentForProviderDto);
		}
		logger.warn(paymentForProvidersDto.size()+" size");
		return paymentForProvidersDto;
	}
	

	 public List<PaymentForProvidersDto> getAllMyAsCompanyByDate(LocalDate date, LocalDate date2, Long id, int page , int pageSize) {
		 LocalDateTime startOfDay = date.atStartOfDay();
		 LocalDateTime endOfDay = date2.atTime(LocalTime.MAX) ;
		 if(date.isAfter(date2)) {
			  endOfDay = date.atStartOfDay(); // 2024-09-07T00:00:00
			  startOfDay = date2.atTime(LocalTime.MAX);
		 }
		 Pageable pageable = PageRequest.of(page, pageSize);
		  Page<PaymentForProviders> payments = paymentForProvidersRepository.findByCreatedDate(startOfDay, endOfDay, id, pageable);
		if(payments.isEmpty()) {
			throw new RecordNotFoundException(null);
		}
		List<PaymentForProvidersDto> paymentsForProvidersDto = new ArrayList<>();
		for(PaymentForProviders i : payments) {
			PaymentForProvidersDto paymentForProvidersDto = paymentForProvidersMapper.mapToDto(i);
			paymentsForProvidersDto.add(paymentForProvidersDto);
		}
		return paymentsForProvidersDto;
	}
	 
	 
	 public String getSumAmountByDate(LocalDate date, LocalDate date2, Long id) {
		 LocalDateTime startOfDay = date.atStartOfDay();
		 LocalDateTime endOfDay = date2.atTime(LocalTime.MAX) ;
 if(date.isAfter(date2)) {
	  endOfDay = date.atStartOfDay(); // 2024-09-07T00:00:00
	  startOfDay = date2.atTime(LocalTime.MAX);
 }
		 BigDecimal amount = paymentForProviderPerDayRepository.getSumAmountByDate(startOfDay, endOfDay,id);
		 logger.warn(amount+" sum is");
		 if(amount == null) {
			 return Integer.toString(0);
		 }
		 return amount.toString();
	 }


	public List<PaymentForProviderPerDayDto> getAllMyProfits(Long id, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<PaymentForProviderPerDay> paymentPerDay = paymentForProviderPerDayRepository.findByProviderId(id, pageable);
		if(paymentPerDay.isEmpty()) {
			throw new RecordNotFoundException("there is no profit yet");
		}
		List<PaymentForProviderPerDayDto> dtos = new ArrayList<>();
		for(PaymentForProviderPerDay i : paymentPerDay ) {
			PaymentForProviderPerDayDto dto = paymentForProviderPerDayMapper.mapToDto(i);
				dtos.add(dto);
		}
		return dtos;
	}


	public List<PaymentForProviderPerDayDto> getAllMyProfitsPerDay(LocalDate date, LocalDate findate, Long id, int page , int pageSize) {
		 LocalDateTime startOfDay = date.atStartOfDay();
		 LocalDateTime endOfDay = findate.atTime(LocalTime.MAX) ;
		 Pageable pageable = PageRequest.of(page, pageSize);
 if(date.isAfter(findate)) {
	  endOfDay = date.atStartOfDay(); // 2024-09-07T00:00:00
	  startOfDay = findate.atTime(LocalTime.MAX);
 }
		Page<PaymentForProviderPerDay> paymentPerDay = paymentForProviderPerDayRepository.findByProviderIdAndDate(startOfDay, endOfDay,id, pageable);
		if(paymentPerDay.isEmpty()) {
			throw new RecordNotFoundException("there is no profit yet between those dates");
		}
		List<PaymentForProviderPerDayDto> dtos = new ArrayList<>();
		for(PaymentForProviderPerDay i : paymentPerDay ) {
			PaymentForProviderPerDayDto dto = paymentForProviderPerDayMapper.mapToDto(i);
				dtos.add(dto);
		}
		return dtos;
	}
}



















