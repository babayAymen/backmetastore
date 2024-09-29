package com.example.meta.store.werehouse.Entities;


import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;

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
@Table(name = "client_company_r")
public class ClientProviderRelation extends BaseEntity<Long> {
	
	@ManyToOne()
	@JoinColumn(name ="providerId")
	private Company provider;

	@ManyToOne()
	@JoinColumn(name = "clientId")
	private Company client;
	
	@ManyToOne()
	@JoinColumn(name = "personId")
	private User person;
	
	private Double mvt;
	
	private Double credit;
	
	private boolean isDeleted;

	private Double advance;
}

