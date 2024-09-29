package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.LikeDto;
import com.example.meta.store.werehouse.Entities.Like;

@Mapper
public interface LikeMapper {

	Like mapToEntity(LikeDto dto);
	
	LikeDto mapToDto(Like entity);
}
