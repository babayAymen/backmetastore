package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;

import com.example.meta.store.werehouse.Dtos.SearchHistoryDto;
import com.example.meta.store.werehouse.Entities.SearchHistory;

@Mapper
public interface SearchHistoryMapper {

	SearchHistory mapToEntity(SearchHistoryDto dto);
	
	SearchHistoryDto mapToDto(SearchHistory entity);
}
