package com.example.meta.store.PointsPayment.Dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentForProviderPerDayDto extends BaseDto<Long> implements Serializable {

	private CompanyDto receiver;
	
	private Boolean isPayed;
	
	private Double amount;
	
	private Double rest;

}
