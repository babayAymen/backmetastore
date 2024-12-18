package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;

@Mapper(componentModel = "spring")
public interface ClientCompanyRMapper {

	ClientProviderRelation mapToEntity(ClientProviderRelationDto dto);
	
	ClientProviderRelationDto mapToDto(ClientProviderRelation entity);
}
