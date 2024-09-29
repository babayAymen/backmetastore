package com.example.meta.store.PointsPayment.Entity;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name="aymenpointspayment")
public class PaymentForAymen  extends BaseEntity<Long> implements Serializable{

	@OneToOne
	@JoinColumn(name = "paymentId")
	private PointsPayment pointpayment;
	
	private Double getenespeces;
	
	private Double giveenespeces;
	
	private Boolean status;
	
}
