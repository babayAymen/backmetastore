package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.Unit;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ArticleCompanyDto extends BaseDto<Long> implements Serializable{

	
	private Double cost;

	private Double quantity;
	
	private Double minQuantity;
	
	private Long sharedPoint;
	
	private Double sellingPrice;
		
	private CategoryDto category;
	
	private SubCategoryDto subCategory;
	
	private CompanyDto provider;
	
	private CompanyDto company;
	
	private Unit unit;
	
	private PrivacySetting isVisible;
	
	private Boolean isEnabledToComment;
	
	private Set<SubArticleRelationDto> subArticle;

	private Boolean isFav;
	
	private Long likeNumber;
	
	private Long commentNumber;
	
	private ArticleDto article;
}
