package com.example.meta.store.PointsPayment.Dto;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseDto;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PaymentForAymenDto extends BaseDto<Long> implements Serializable{

	
	private PointsPaymentDto pointpayment;
	
	private Double getenespeces;
	
	private Double giveenespeces;
	
	private Boolean status;
}
