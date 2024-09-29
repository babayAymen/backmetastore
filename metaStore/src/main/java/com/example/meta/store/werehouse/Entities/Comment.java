package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;
import java.util.Set;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
@Table(name="comment")
public class Comment extends BaseEntity<Long> implements Serializable {
	
	private String content;
		
	  @ManyToOne
	  @JoinColumn(name = "userId")
	    private User user;
	    
	    @ManyToOne
	    @JoinColumn(name = "companyId")
	    private Company companie;
	    
	    @ManyToOne
	    @JoinColumn(name = "articleId")
	    private ArticleCompany article;

}
