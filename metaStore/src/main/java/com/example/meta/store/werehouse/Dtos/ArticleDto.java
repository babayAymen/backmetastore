package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.Unit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleDto extends BaseDto<Long> implements Serializable {


    private static final long serialVersionUID = 12345678117L;
    
	private String libelle;
	
	private String code;
	private String image;
	private String barcode;
	private String discription;
	private Double tva;
	private Boolean isDiscounted;
	private CompanyCategory category;
	

	

}
