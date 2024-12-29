package com.example.meta.store.PointsPayment.Entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.werehouse.Entities.Company;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "provider_per_day")
public class PaymentForProviderPerDay extends BaseEntity<Long> implements Serializable {

	@ManyToOne
	@JoinColumn(name = "receiverId")
	private Company receiver;
	
	private Boolean isPayed;
	
	private Double amount;
	
	private Double rest;
	
	
}
