package com.example.meta.store.werehouse.Dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceReturnDto {

	private Long id;
	
	private String name;

	private String phone;
	
	private String address;
	
	private String matfisc;
	
	private String indestrySector;
	
	private String email;
	
	private Boolean paid;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
}

