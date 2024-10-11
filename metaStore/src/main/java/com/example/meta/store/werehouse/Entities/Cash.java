package com.example.meta.store.werehouse.Entities;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import com.example.meta.store.werehouse.Enums.PaymentStatus;

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
@Entity
@Table(name = "cash")
@NoArgsConstructor
@AllArgsConstructor
public class Cash extends BaseEntity<Long> {

	private Double amount;
	
	private PaymentStatus status;
	@ManyToOne
	@JoinColumn(name = "invoiceId")
	private Invoice invoice;
}
