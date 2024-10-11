package com.example.meta.store.Base.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDto<ID>  implements Serializable{

    private static final long serialVersionUID = 1234567812L;
    
	private ID id;
	
	private String statusCode;
	
	private boolean isDeleted;
	
	private Long createdBy;
	
	private LocalDateTime createdDate;
	
	private Long lastModifiedBy;
	
	private LocalDateTime lastModifiedDate;

}
