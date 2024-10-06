package com.example.meta.store.werehouse.Dtos;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnableToCommentDto extends BaseDto<Long> {

	private CompanyDto raterCompany;
	
	private CompanyDto rateeCompany;
	
	private UserDto user;
	
	private Boolean enable_client_Article;
	
	private Boolean enable_client_company;
	
	private Boolean enable_provider;
}
