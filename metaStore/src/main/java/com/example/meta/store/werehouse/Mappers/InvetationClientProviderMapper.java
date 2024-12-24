package com.example.meta.store.werehouse.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.meta.store.werehouse.Dtos.InvetationDto;
import com.example.meta.store.werehouse.Dtos.WorkerDto;
import com.example.meta.store.werehouse.Entities.Invetation;

@Mapper(componentModel = "spring")
public interface InvetationClientProviderMapper {

	Invetation mapToEntity(InvetationDto dto);
	

    @Mapping(source = "client.image", target = "client.image")
	InvetationDto mapToDto(Invetation entity);
	

   @Mapping(source = "client.username", target = "name")
   @Mapping(source = "client.address", target = "address")
   @Mapping(source = "client.email", target = "email")
   @Mapping(source = "client.phone", target = "phone")
   @Mapping(source = "client", target = "user")
	WorkerDto mapInvetationToWorker(Invetation entity);
}
