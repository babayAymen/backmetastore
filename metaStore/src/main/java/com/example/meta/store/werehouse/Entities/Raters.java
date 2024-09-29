package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.RateType;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "raters")
public class Raters extends BaseEntity<Long> implements Serializable{

	@ManyToOne
	@JoinColumn(name = "reterUserId")
	private User raterUser;
	
	@ManyToOne
	@JoinColumn(name = "reteeUserId")
	private User rateeUser;
	
	@ManyToOne
	@JoinColumn(name = "reterCompanyId")
	private Company raterCompany;
	
	@ManyToOne
	@JoinColumn(name = "reteeCompanyId")
	private Company rateeCompany;
	
	private String comment;
	
	private String photo;
	
	private Double rateValue;
	
	private RateType type;
	
}
