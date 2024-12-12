package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.Unit;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="article_company")
public class ArticleCompany extends BaseEntity<Long> implements Serializable{

	private Unit unit;
	@PositiveOrZero(message = "Cost Must Be A Positive Number")
	private Double cost;
	@PositiveOrZero(message = "Quantity Must Be A Positive Number")
	private Double quantity;
	private Double minQuantity;
	@PositiveOrZero(message = "Selling_Price Must Be A Positive Number")
	private Double sellingPrice;
	private Long sharedPoint;
	private PrivacySetting isVisible;
	@ManyToOne(optional = true)
	@JoinColumn(name = "categoryId")
	private Category category;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "subCategoryId")
	private SubCategory subCategory;
	
	@ManyToOne()
	@JoinColumn(name = "providerId")
	private Company provider;
	
	@OneToMany(mappedBy = "parentArticle")
	private Set<SubArticle> subArticle;
	
	private Long likeNumber;
	private Long commentNumber;
	@ManyToMany
	 @JoinTable(
		        name = "like_article_company",
		        joinColumns = @JoinColumn(name = "articleId"),
		        inverseJoinColumns = @JoinColumn(name = "companyId")
		    )
	private Set<Company> companies ;
	@ManyToMany
	 @JoinTable(
		        name = "like_article_user",
		        joinColumns = @JoinColumn(name = "articleId"),
		        inverseJoinColumns = @JoinColumn(name = "userId")
		    )
	private Set<User> users;
	
	@ManyToOne
	@JoinColumn(name= "companyId")
	private Company company;
	@ManyToOne
	@JoinColumn(name = "articleId")
	private Article article;
	
	private Boolean isDeleted;
}
