package com.example.meta.store.PointsPayment.Entity;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;

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
@Table(name="providerspointspayment")
public class PaymentForProviders extends BaseEntity<Long> implements Serializable {

	@OneToOne
	private PurchaseOrderLine purchaseOrderLine;
	
	private Double giveenespece;
	
	private Boolean status;
	
}
