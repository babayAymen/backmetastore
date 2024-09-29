package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Enums.Unit;

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
@Table(name="search")
public class SearchHistory extends BaseEntity<Long> implements Serializable {
	
	private SearchCategory searchCategory;
	
	@ManyToOne
	private Company company;
	@ManyToOne
	private Article article;
	@ManyToOne
	private User user;
	
	@ManyToOne()
	@JoinColumn(name = "meUserId")
	private User meUser;
	
	@ManyToOne
	@JoinColumn(name = "meCompanyId")
	private Company meCompany;
	
	
	
}
