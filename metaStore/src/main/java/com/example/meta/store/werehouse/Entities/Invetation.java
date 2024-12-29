package com.example.meta.store.werehouse.Entities;
 
import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Enums.Type;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name="invetation")
public class Invetation extends BaseEntity<Long> {


    private static final long serialVersionUID = 1234567810878L;
	@ManyToOne()
	private User client;
	
	@ManyToOne()
	@JoinColumn(name="company_sender_id")
	private Company companySender;
	
	@ManyToOne()
	@JoinColumn(name="company_reciver_id")
	private Company companyReceiver;
	
	private Double salary;
	
	private String jobtitle;
	
	private String department;
	
	private long totdayvacation;

	private boolean statusvacation;
	
	private Status status;

	private Type type;
}
