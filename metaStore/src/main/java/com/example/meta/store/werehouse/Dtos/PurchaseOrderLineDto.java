package com.example.meta.store.werehouse.Dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseDto;
import com.example.meta.store.werehouse.Entities.PurchaseOrder;
import com.example.meta.store.werehouse.Enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderLineDto extends BaseDto<Long> implements Serializable {

	
	private ArticleCompanyWithoutTroubleDto article;
	
	private Double quantity;
	
	private String comment;
	
	private Status status;
	
	private Boolean delivery;

	private Double totTva;
	
	private Double prixArticleTot;
	
	private PurchaseOrderDto purchaseorder;
	
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime createdDate;
	  
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime lastModifiedDate;
	  
	  private InvoiceDto invoice;
}
