package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.meta.store.werehouse.Dtos.ArticleDto;
import com.example.meta.store.werehouse.Entities.Article;

@Mapper(componentModel = "spring")
public interface ArticleMapper {


    Article mapToEntity(ArticleDto dto);

    ArticleDto mapToDto(Article entity);
    

}
