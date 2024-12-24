package com.example.meta.store.Base.AppConfig;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.meta.store.Base.Security.Entity.Role;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.RoleService;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Entities.Delivery;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Services.DeliveryService;


@Component
public class StartUpApp implements CommandLineRunner {

	@Autowired
	private RoleService roleService;
	
	@Autowired
	private UserService appUserService;
	
	@Autowired
	private DeliveryService deliveryService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public void run(String... args) throws Exception {
		Optional<User> user = appUserService.findById(1L);
		if(user.isEmpty()) {
		insertUser();
		insertDelivery();
		}
	}
	

	
	public ResponseEntity<?> insertUser(){
		
		User user = new User("+21697396321","aymen babay","aymen1@gmail.com",passwordEncoder.encode("password_meta_2024"),RoleEnum.USER, AccountType.META);
		
		return appUserService.insert(user);
	}
	
	public void insertDelivery() {
		User user = appUserService.findById((long)1).get();
		Delivery aymen = new Delivery(user, null, null);
		deliveryService.insert(aymen);
		
	}
}
