package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.CompanyCategory;

import jakarta.persistence.Entity;
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
@Table(name="enable_to_comment")
public class EnableToComment extends BaseEntity<Long> implements Serializable {

	private Company raterCompany;
	
	private Company rateeCompany;
	
	private User user;
	
	private Boolean enable_client_Article;
	
	private Boolean enable_client_company;
	
	private Boolean enable_provider;
	
}
