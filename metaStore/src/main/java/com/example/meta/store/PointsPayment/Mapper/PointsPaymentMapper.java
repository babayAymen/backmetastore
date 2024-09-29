package com.example.meta.store.PointsPayment.Mapper;

import org.mapstruct.Mapper;

import com.example.meta.store.PointsPayment.Dto.PointsPaymentDto;
import com.example.meta.store.PointsPayment.Entity.PointsPayment;

@Mapper
public interface PointsPaymentMapper {
	
	PointsPayment mapToEntity(PointsPaymentDto dto);
	
	PointsPaymentDto mapToDto(PointsPayment entity);

}
