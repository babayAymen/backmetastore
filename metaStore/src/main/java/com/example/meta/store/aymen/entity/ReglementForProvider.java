package com.example.meta.store.aymen.entity;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.PointsPayment.Entity.PaymentForProviderPerDay;
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
@Table(name="reglement_for_provider")
public class ReglementForProvider extends BaseEntity<Long> implements Serializable {

	@ManyToOne
	@JoinColumn(name = "payerId")
	private Company payer;
//	
//	@ManyToOne
//	@JoinColumn(name = "receiverId")
//	private Company receiver;
	
	private Double amount;
	
	private Boolean isAccepted;
	
	@ManyToOne
	@JoinColumn(name = "metaId")
	private User meta;
	
	@ManyToOne
	@JoinColumn(name = "par_day_id")
	private PaymentForProviderPerDay paymentForProviderPerDay;
}
