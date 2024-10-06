package com.example.meta.store.werehouse.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.RateController;
import com.example.meta.store.werehouse.Dtos.RatersDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Raters;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.RateType;
import com.example.meta.store.werehouse.Mappers.RatersMapper;
import com.example.meta.store.werehouse.Repositories.RateresRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@Transactional
@RequiredArgsConstructor
public class RateService extends BaseService<Raters, Long> {
	
	
	private final CompanyService companyService;
	
	private final RateresRepository rateresRepository;
	
	private final UserService userService;
	
	private final RatersMapper ratersMapper;
	
	private final ObjectMapper objectMapper;

	private final ImageService imageService;
	private final Logger logger = LoggerFactory.getLogger(RateService.class);
	
	public void rate( String rates, User myUser, Company myCompany, MultipartFile image) throws JsonMappingException, JsonProcessingException {
		logger.warn(rates);
		Raters ratesDto = objectMapper.readValue(rates, Raters.class);
		Raters raters = new Raters();
		raters.setComment(ratesDto.getComment());
		raters.setType(ratesDto.getType());
		raters.setRateValue(ratesDto.getRateValue());
		if(image != null) {
			String newFileName = imageService.insertImag(image,myUser.getId(), "rating");
			raters.setPhoto(newFileName);
		}
		switch (ratesDto.getType()) {
		case COMPANY_RATE_COMPANY: {
			Company company = companyService.getById(ratesDto.getRateeCompany().getId()).getBody();
			raters.setRateeCompany(company);
			raters.setRaterCompany(myCompany);
			Double rat = calculRate((double)company.getRaters(), company.getRate(), ratesDto.getRateValue());
			company.setRate(rat);
			company.setRaters(company.getRaters()+1);
			break;
		}
		case COMPANY_RATE_USER: {
			User user = userService.findById(ratesDto.getRateeUser().getId()).get();
			logger.warn(user.getRate()+" rate "+user.getRater()+" rater");
			raters.setRateeUser(user);
			raters.setRaterCompany(myCompany);
			Double rat = calculRate((double)user.getRater(), user.getRate(), ratesDto.getRateValue());
			user.setRate(rat);
			user.setRater(user.getRater()+1);
			break;
		}
		case USER_RATE_COMPANY: {
			Company company = companyService.getById(ratesDto.getRateeCompany().getId()).getBody();
			raters.setRateeCompany(company);
			raters.setRaterUser(myUser);
			Double rat = calculRate((double)company.getRaters(), company.getRate(), ratesDto.getRateValue());
			company.setRate(rat);
			company.setRaters(company.getRaters()+1);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + ratesDto.getType());
		}
		rateresRepository.save(raters);
		
	}
	
	private double calculRate(Double rater, Double rate, Double value  ) {
		return divideWithTwoValue(
				sumWithTwoValue(
				multipleWithTwoValue(
						rater, rate) 
				,value
				),
				sumWithTwoValue(rater,1.0));
	}

	public List<RatersDto> getAllById(Long id, AccountType type) {
		List<Raters> raters = new ArrayList<>();
		logger.warn(" id raters "+id);
		if(type.equals(AccountType.COMPANY)) {			
		 raters = rateresRepository.findAllByRateeCompanyId(id);
			logger.warn("size "+raters.size()+" size raters "+id);
		}else {			
		raters = rateresRepository.findAllByRateeUserId(id);
		logger.warn(" id raters from user  "+id);
		}
		if(raters.isEmpty()) {
			throw new RecordNotFoundException(type.toString());
		}
		List<RatersDto> ratersDto = new ArrayList<>();
		for(Raters i : raters) {
			RatersDto dto = ratersMapper.mapToDto(i);
			ratersDto.add(dto);
		}
		logger.warn(ratersDto.size()+" size raters "+id);
		return ratersDto;
	}
	private void rateCompany(Company company, double rate) {
		Double rates = divideWithTwoValue(multipleWithTwoValue(company.getRate(),company.getRaters()+rate),sumWithTwoValue((double)company.getRaters(),1.0));
		company.setRate(rates);
		company.setRaters(company.getRaters()+1);
	}
	
	private Double multipleWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.multiply(val3);//gf
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	private Double divideWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		 BigDecimal val4 = val.divide(val3, 2, RoundingMode.HALF_UP);  // Specify scale and rounding mode during division
		    return val4.doubleValue();
	}
	private Double sumWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.add(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public Boolean enableToCommentCompany(Long mycompanyId, Long userId, Long companyId) {
		
		return null;
	}

}
