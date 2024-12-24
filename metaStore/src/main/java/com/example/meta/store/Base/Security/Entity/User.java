package com.example.meta.store.Base.Security.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Enums.AccountType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Appuser")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Transactional
public class User extends BaseEntity<Long> implements UserDetails, Serializable {


    private static final long serialVersionUID = 1234567819L;
		 
			private String phone;
			
			private String address;
			
			@NotBlank(message = "User Name Field Must Be Not Empty")
			private String username;
			
			private String email;

			@NotBlank(message = "Password Field Must Be Not Empty")
			private String password;
			
			private String resettoken;
			
			private LocalDateTime datetoken;
			
			private double longitude;
			
			private double latitude;

		    @OneToMany(mappedBy = "person")
		    private Set<ClientProviderRelation> companiesR;
			
//			@ManyToMany(fetch = FetchType.EAGER)
//			@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name ="userId"), 
//			inverseJoinColumns = @JoinColumn(name="roleId"))
			private RoleEnum role;
			private AccountType accountType;
		
			private Double balance;
			
			private String image;
			
			private  Double rate;
			
			private int rater;
			

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {

			    return Set.of(new SimpleGrantedAuthority(role.name()));
//			    return role 
//			    		.stream()
//			            .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
//			            .collect(Collectors.toList())
			            
			}
			
			@Override
			public String getPassword() {
				return password;
			}
			@Override
			public String getUsername() {
				// TODO Auto-generated method stub
				return username;
			}

			@Override
			public boolean isAccountNonExpired() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean isAccountNonLocked() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean isCredentialsNonExpired() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean isEnabled() {
				// TODO Auto-generated method stub
				return true;
			}

			public User(String phone, String username, String email, String password, RoleEnum role, AccountType type) {
				super();
				this.phone = phone;
				this.username = username;
				this.email = email;
				this.password = password;
				this.role = role;
				this.balance = 0.0;
				this.accountType = type;
			}

			


}
