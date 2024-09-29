package com.example.meta.store.PointsPayment.Dto;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Dtos.PurchaseOrderLineDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentForProvidersDto extends BaseDto<Long> {

	
	private PurchaseOrderLineDto purchaseOrderLine;
	
	private Double giveenespece;
	
	private Boolean status;
}
