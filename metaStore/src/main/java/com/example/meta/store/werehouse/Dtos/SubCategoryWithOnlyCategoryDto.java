package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubCategoryWithOnlyCategoryDto extends BaseDto<Long> implements Serializable {
	
    
	private String code;

	private String libelle;
	
	private String image;
	
	private OnlyCategoryDto category;

}
