package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.ArticleCompanyDto;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyWithoutTroubleDto;
import com.example.meta.store.werehouse.Entities.ArticleCompany;

@Mapper(componentModel = "spring")
public interface ArticleCompanyMapper {

	ArticleCompany mapToEntity(ArticleCompanyDto dto);
	
	ArticleCompanyDto mapToDto(ArticleCompany entity);
	
	ArticleCompanyWithoutTroubleDto mapToArticleCompanyWithoutTroubleDto(ArticleCompany entity);
}
