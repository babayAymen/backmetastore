package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.CompanyCategory;

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
@Table(name="enable_to_comment")
public class EnableToComment extends BaseEntity<Long> implements Serializable {


    @ManyToOne
    @JoinColumn(name = "rater_company_id")
	private Company raterCompany;

    @ManyToOne
    @JoinColumn(name = "ratee_company_id")
	private Company rateeCompany;

    @ManyToOne
    @JoinColumn(name = "user_id")
	private User user;
	
	private Boolean enableClientArticle;
	
	private Boolean enableClientCompany;
	
	private Boolean enableProvider;
	
}
