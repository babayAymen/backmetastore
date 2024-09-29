package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name="article")
public class Article extends BaseEntity<Long> implements Serializable{


    private static final long serialVersionUID = 12345678108L;
    
	@NotBlank(message = "Libelle Field Must Not Be Empty")
	private String libelle;
	
	@NotBlank(message = "Code Field Must Not Be Empty")
	private String code;

	
	private String discription;
	private Boolean isDiscounted;
	private String image;
	private String barcode;
	@PositiveOrZero(message = "TVA Must Be A Positive Number")
	private Double tva;
	private CompanyCategory category;
	
	
}
