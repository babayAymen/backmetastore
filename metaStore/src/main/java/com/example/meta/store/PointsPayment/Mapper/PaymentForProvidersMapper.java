package com.example.meta.store.PointsPayment.Mapper;

import org.mapstruct.Mapper;

import com.example.meta.store.PointsPayment.Dto.PaymentForProvidersDto;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviders;

@Mapper(componentModel = "spring")
public interface PaymentForProvidersMapper {

	
	PaymentForProviders mapToEntity(PaymentForProvidersDto dto);
	
	PaymentForProvidersDto mapToDto(PaymentForProviders entity);
}
