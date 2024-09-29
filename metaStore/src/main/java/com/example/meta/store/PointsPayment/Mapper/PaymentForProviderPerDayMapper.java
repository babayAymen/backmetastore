package com.example.meta.store.PointsPayment.Mapper;

import org.mapstruct.Mapper;

import com.example.meta.store.PointsPayment.Dto.PaymentForProviderPerDayDto;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviderPerDay;

@Mapper
public interface PaymentForProviderPerDayMapper {

	PaymentForProviderPerDayDto mapToDto(PaymentForProviderPerDay entity);
	
	PaymentForProviderPerDay mapToEntity(PaymentForProviderPerDayDto dto);
}
