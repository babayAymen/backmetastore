package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Entities.Company;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class WorkerDto extends BaseDto<Long> implements Serializable {
	

    private static final long serialVersionUID = 12345678126L;
    
	private String name;
	
	private String phone;
	
	private String email;
	
	private String address;
	
	private Double salary;
	
	private String jobtitle;
	
	private String department;
	
	private long totdayvacation;

	private long remainingday;
	
	private boolean statusvacation;
	
	private UserDto user;

	private CompanyDto company;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
}
