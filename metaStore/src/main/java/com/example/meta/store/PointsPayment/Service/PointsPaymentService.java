package com.example.meta.store.PointsPayment.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.PointsPayment.Dto.PointsPaymentDto;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviderPerDay;
import com.example.meta.store.PointsPayment.Entity.PointsPayment;
import com.example.meta.store.PointsPayment.Mapper.PointsPaymentMapper;
import com.example.meta.store.PointsPayment.Repository.PaymentForProviderPerDayRepository;
import com.example.meta.store.PointsPayment.Repository.PointPaymentRepository;
import com.example.meta.store.aymen.dto.ReglementForProviderDto;
import com.example.meta.store.aymen.entity.ReglementForProvider;
import com.example.meta.store.aymen.mapper.ReglementForProviderMapper;
import com.example.meta.store.aymen.repository.ReglementForProviderRepository;
import com.example.meta.store.werehouse.Controllers.ArticleController;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Services.CompanyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Service
@Transactional
public class PointsPaymentService extends BaseService<PointsPayment, Long> {


	private final PointsPaymentMapper pointsPaymentMapper;
	private final ReglementForProviderMapper reglementForProviderMapper;
	
	private final PointPaymentRepository pointPaymentRepository;
	private final ReglementForProviderRepository reglementForProviderRepository;
	private final PaymentForProviderPerDayRepository paymentForProviderPerDayRepository;
	
	private final CompanyService companyService;
	
	private final UserService userService;

	private final PaymentForAymentService paymentForAymentService;
	

	private final Logger logger = LoggerFactory.getLogger(PointsPaymentService.class);
	
	
	public void sendPoints(PointsPaymentDto pointPaymentDto, Company myCompany) {
		PointsPayment pointPayment = pointsPaymentMapper.mapToEntity(pointPaymentDto);
		pointPayment.setProvider(myCompany);
		if(pointPaymentDto.getClientCompany().getId() != null) {
			Company company = companyService.getById(pointPaymentDto.getClientCompany().getId()).getBody();
			Double balance = sumWithTwoValue(company.getBalance(), multipleWithTwoValue(pointPaymentDto.getAmount(), 1.1));
			company.setBalance(balance);
			pointPayment.setClientUser(null);
	}else {
		User user = userService.findById(pointPaymentDto.getClientUser().getId()).get();
		pointPayment.setClientUser(user);
		pointPayment.setClientCompany(null);
		Double balance = sumWithTwoValue(user.getBalance(), multipleWithTwoValue(pointPaymentDto.getAmount(), 1.1));
		user.setBalance(balance);
	}
		pointPaymentRepository.save(pointPayment);
		paymentForAymentService.insertPayment(pointPayment);
		
	}

	public List<PointsPaymentDto> getAllMyPointsPayment(Long companyId, Long userId, int page , int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC, "lastModifiedDate");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		List<PointsPaymentDto> pointsPaymentDto = new ArrayList<>();
		logger.warn("company id : "+companyId + " user id : "+userId);
		Page<PointsPayment> pointsPayment = pointPaymentRepository.findAllByCompanyIdOrUserId(companyId, userId, pageable);
		for(PointsPayment i : pointsPayment) {
			PointsPaymentDto dto = pointsPaymentMapper.mapToDto(i);
			pointsPaymentDto.add(dto);
		}
		logger.warn(pointsPaymentDto.size()+" size array");
		return pointsPaymentDto;
	}
	
	private Double multipleWithTwoValue(Long val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.multiply(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	private Double sumWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.add(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public void sendReglement(ReglementForProviderDto reglementDto) {
		logger.warn(reglementDto+"");
		ReglementForProvider reglement = reglementForProviderMapper.mapToEntity(reglementDto);
		reglementForProviderRepository.save(reglement);
		PaymentForProviderPerDay paymentForProviderPerDay = paymentForProviderPerDayRepository.findById(reglement.getPaymentForProviderPerDay().getId()).get();
			BigDecimal paymentAmount = new BigDecimal(paymentForProviderPerDay.getRest());
			BigDecimal reglementAmount = new BigDecimal(reglement.getAmount());
			BigDecimal deff = paymentAmount.subtract(reglementAmount);
			var compere = paymentAmount.compareTo(reglementAmount);
			if(compere == 1) {
				paymentForProviderPerDay.setRest(deff.setScale(2,RoundingMode.HALF_UP).doubleValue());
			}else {
				paymentForProviderPerDay.setRest(0.0);
				paymentForProviderPerDay.setIsPayed(true);
			}
			paymentForProviderPerDayRepository.save(paymentForProviderPerDay);
			
		
	}
}







