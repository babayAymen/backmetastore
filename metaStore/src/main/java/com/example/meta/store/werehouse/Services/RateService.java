package com.example.meta.store.werehouse.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	
	private final EnableToCommentService enableToCommentService;
	
	private final RatersMapper ratersMapper;
	
	private final ObjectMapper objectMapper;

	private final ImageService imageService;

	
	private final Logger logger = LoggerFactory.getLogger(RateService.class);
	
	public RatersDto rate( String rates, User myUser, Company myCompany, MultipartFile image) throws JsonMappingException, JsonProcessingException {
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
			enableToCommentService.makeDisableToCommentCompany(company.getId(),myCompany.getId(), null);
			break;
		}
		case COMPANY_RATE_USER: {
			User user = userService.findById(ratesDto.getRateeUser().getId()).get();
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
			enableToCommentService.makeDisableToCommentCompany(company.getId(),null, myUser.getId());
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + ratesDto.getType());
		}
		rateresRepository.save(raters);
		RatersDto response = ratersMapper.mapToDto(raters);
		return response;
		
	}
	
	private double calculRate(Double rater, Double rate, Double value  ) {
		return divideWithTwoValue(
				sumWithTwoValue(
				multipleWithTwoValue(rater, rate) 
				,value
				),
				sumWithTwoValue(rater,1.0));
	}

	public Page<RatersDto> getAllById(Long id, AccountType type, int page , int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC,"lastModifiedDate");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Page<Raters> raters = Page.empty();
		if(type.equals(AccountType.COMPANY)) {			
		 raters = rateresRepository.findAllByRateeCompanyId(id, pageable);
		}else {			
		raters = rateresRepository.findAllByRateeUserId(id, pageable);
		logger.warn(" id raters from user  "+id);
		}
		List<RatersDto> ratersDto = raters.stream()
				.map(ratersMapper::mapToDto)
				.toList();
		logger.warn(ratersDto.size()+" size raters "+id);
		return new PageImpl<>(ratersDto, pageable, raters.getTotalElements());
	}

	private Double multipleWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.multiply(val3);
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

	public Boolean enableToCommentCompany(Long myCompanyId, Long userId, Long companyId) {
		Boolean exists = enableToCommentService.existByUserIdAndCompanyId(myCompanyId , userId, companyId);
		logger.warn("return is : "+exists);
		return exists;
	}

	public Boolean enableToCommentUser(Long id, Long userId) {
		Boolean exists = enableToCommentService.existsByUserIdAndCompanyId(id , userId);
		logger.warn("return is : "+exists);
		return exists;
	}

	public Boolean enableToCommentArticle(Long companyId, Long myUserId, Long myCompanyId) {
		
		return enableToCommentService.enableToCommentArticle(companyId,myUserId,myCompanyId);
	}

}
