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
@Table(name="_like")
public class Like extends BaseEntity<Long> implements Serializable {


    private static final long serialVersionUID = 123456781084L;
       
    @ManyToMany
    @JoinTable(
            name = "user_likes_article",
            joinColumns = @JoinColumn(name = "_like_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
        )
    private Set<User> users;
    
    @ManyToMany
    @JoinTable(
            name = "company_likes_article",
            joinColumns = @JoinColumn(name = "_like_id"),
            inverseJoinColumns = @JoinColumn(name = "company_id")
        )
    private Set<Company> companies;
    
    @OneToOne
    @JoinColumn(name = "articleId")
    private Article article;
}
