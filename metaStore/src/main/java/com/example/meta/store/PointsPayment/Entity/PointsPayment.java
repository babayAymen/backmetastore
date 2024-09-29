package com.example.meta.store.PointsPayment.Entity;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
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
@Table(name="pointspayment")
public class PointsPayment extends BaseEntity<Long> implements Serializable {
	
	private Long amount;
	
	@ManyToOne
	@JoinColumn(name = "providerId")
	private Company provider;
	
	@ManyToOne
	@JoinColumn(name = "companyId")
	private Company clientCompany;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	private User clientUser;

}
