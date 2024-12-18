package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.RatersDto;
import com.example.meta.store.werehouse.Entities.Raters;

@Mapper(componentModel = "spring")
public interface RatersMapper {

	Raters mapToEntity(RatersDto dto);
	
	RatersDto mapToDto(Raters entituy);
}
