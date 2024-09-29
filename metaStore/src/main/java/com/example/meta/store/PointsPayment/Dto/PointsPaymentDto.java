package com.example.meta.store.PointsPayment.Dto;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.Company;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PointsPaymentDto extends BaseDto<Long> implements Serializable{

	
	private Long amount;
	
	private CompanyDto provider;
	
	private CompanyDto clientCompany;
	
	private UserDto clientUser;
}
