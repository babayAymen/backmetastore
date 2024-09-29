package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Enums.PaymentMode;
import com.example.meta.store.werehouse.Enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto extends BaseDto<Long> implements Serializable {

	private Double amount;
	
	private LocalDateTime delay;
	
	private String agency;

	private String bankAccount;
	
	private String number;
	
	private String transactionId;
	
	private Status status;

	private PaymentMode type;
	
	private InvoiceDto invoice;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
}
