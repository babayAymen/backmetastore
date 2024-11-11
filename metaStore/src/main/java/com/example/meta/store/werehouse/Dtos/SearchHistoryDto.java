package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SearchHistoryDto extends BaseDto<Long> implements Serializable {

	
	private SearchCategory searchCategory;
	
	private CompanyDto company;

	private ArticleCompanyDto article;

	private UserDto user;
	
	private UserDto meUser;
	
	private CompanyDto meCompany;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
}
