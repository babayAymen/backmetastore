package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.EnableToCommentDto;
import com.example.meta.store.werehouse.Entities.EnableToComment;

@Mapper(componentModel = "spring")
public interface EnableToCommentMapper {

	EnableToComment mapToEntity(EnableToCommentDto dto);
	
	EnableToCommentDto mapToDto(EnableToComment entity);
}
