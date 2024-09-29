package com.example.meta.store.PointsPayment.Dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentForProviderPerDayDto extends BaseDto<Long> implements Serializable {

	private CompanyDto provider;
	
	private Boolean payed;
	
	private Double amount;

}
