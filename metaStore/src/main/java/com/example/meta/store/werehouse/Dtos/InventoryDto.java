package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.Company;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class InventoryDto extends BaseDto<Long> implements Serializable {


    private static final long serialVersionUID = 12345678121L;

	private Double current_quantity;
	
	private Double out_quantity;
	
	private Double in_quantity; 
		
	private String bestClient;
	
	private Double articleCost;
	
	private Double articleSelling;
	
	private Double discountOut;
	
	private Double discountIn;
	
	private CompanyDto company;
	
	private ArticleCompanyDto article;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
}
