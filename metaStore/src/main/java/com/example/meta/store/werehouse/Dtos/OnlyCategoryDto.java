package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnlyCategoryDto extends BaseDto<Long> implements Serializable {
	private Long id ;
	private String code;

	private String libelle;
	
	private String image;

}
