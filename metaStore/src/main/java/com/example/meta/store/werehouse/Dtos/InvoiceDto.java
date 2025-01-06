package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.InvoiceDetailsType;
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class InvoiceDto extends BaseDto<Long> implements Serializable {


    private static final long serialVersionUID = 123456781022L;
    
	private Long code;
	private Double tot_tva_invoice;
	private Double prix_invoice_tot;
	private Double prix_article_tot;
	private Double discount;
	private Status status;
	
	private PaymentStatus paid;

	private InvoiceDetailsType type;
	
	private Boolean isEnabledToComment;
	
	private UserDto person;
	
	private CompanyDto client;
	
	private CompanyDto provider;

//	private String LastModifiedBy;
	
	private Double rest;
	
}
