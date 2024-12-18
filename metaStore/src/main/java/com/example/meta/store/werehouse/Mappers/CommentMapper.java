package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.CommentDto;
import com.example.meta.store.werehouse.Entities.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

	Comment mapToEntity(CommentDto dto);
	
	CommentDto mapToDto(Comment entity);
	
}
