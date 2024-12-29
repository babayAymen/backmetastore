package com.example.meta.store.aymen.dto;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.PointsPayment.Dto.PaymentForProviderPerDayDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReglementForProviderDto extends BaseDto<Long> implements Serializable {

	private CompanyDto payer;
	
//	private CompanyDto receiver;
	
	private Double amount;
	
	private Boolean isAccepted;
	
	private UserDto meta;

	private PaymentForProviderPerDayDto paymentForProviderPerDay;
}
