package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import com.example.meta.store.werehouse.Enums.InvoiceType;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyDto extends BaseDto<Long> implements Serializable {

    private static final long serialVersionUID = 12345678102L;
    
    private Long id ;
    
	private String name;
	
	private String code;
		
	private String matfisc;
	
	private String address;
	
	private String phone;
	
	private String bankaccountnumber;
	
	@Email
	private String email;

//	private CompanyCategory indestrySector;
	
	private String capital;
	
	private String logo;
	
	private int workForce;

    private boolean isVirtual;
		
	private double rate;
	
	private int raters;

	private PrivacySetting isVisible;
	
	private UserDto user;

	private Set<CompanyDto> branshes;

	private CompanyDto parentCompany;

	private ClientProviderRelationDto clientcompany;
	
	private ClientProviderRelationDto providercompany;

	private CompanyCategory category;

	private Double balance;
	
	private Boolean isPointsSeller;

	private Boolean metaSeller;
	
	private Double longitude;
	
	private Double latitude;
	
	private InvoiceType invoiceType;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
}
