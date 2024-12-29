package com.example.meta.store.aymen.mapper;

import org.mapstruct.Mapper;

import com.example.meta.store.aymen.dto.ReglementForProviderDto;
import com.example.meta.store.aymen.entity.ReglementForProvider;

@Mapper(componentModel = "spring")
public interface ReglementForProviderMapper {

	ReglementForProvider mapToEntity(ReglementForProviderDto dto);
	
	ReglementForProviderDto mapToDto(ReglementForProvider entity);
	
}
