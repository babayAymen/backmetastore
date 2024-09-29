package com.example.meta.store.PointsPayment.Mapper;

import org.mapstruct.Mapper;

import com.example.meta.store.PointsPayment.Dto.PaymentForAymenDto;
import com.example.meta.store.PointsPayment.Entity.PaymentForAymen;

@Mapper
public interface PaymentForAymenMapper {

	PaymentForAymen mapToEntity(PaymentForAymenDto dto);
	
	PaymentForAymenDto mapToDto(PaymentForAymen entity);
}
