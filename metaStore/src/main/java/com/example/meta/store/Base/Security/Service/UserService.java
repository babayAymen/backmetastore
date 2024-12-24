package com.example.meta.store.Base.Security.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordIsAlreadyExist;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Config.JwtService;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.AuthenticationRequest;
import com.example.meta.store.Base.Security.Entity.AuthenticationResponse;
import com.example.meta.store.Base.Security.Entity.RegisterRequest;
import com.example.meta.store.Base.Security.Entity.Role;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Mappers.UserMapper;
import com.example.meta.store.Base.Security.Repository.UserRepository;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private static final long EXPIRE_TOKEN_AFTER_MINUTES = 60;
	
	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	private final JwtService jwtService;
	
	private final AuthenticationManager authenticationManager;
	
	private final RoleService roleService;
	
	private final UserMapper userMapper;

	private final JwtAuthenticationFilter authenticationFilter;


	private final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	public List<User> findAll(){
		return userRepository.findAll();
	}
	
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}
	

	@CacheEvict(value = "user", key = "#root.methodName", allEntries = true)
	public ResponseEntity<User> insert(User user) {
		return ResponseEntity.ok(userRepository.save(user));
	}

	//@Cacheable(value = "user", key = "#root.methodName + '_'+ #name")
	public UserDto getMyUser(String name) {
		
		Optional<User> user = userRepository.findByUsername(name);
		if(user.isEmpty()) {
			throw new RecordNotFoundException("there is no User with name "+name);
		}
		UserDto userDto = userMapper.mapToDto(user.get());
		return userDto;
	}
	
	public User findByUserName(String name) {
		
		Optional<User> user = userRepository.findByUsername(name);
		if(user.isEmpty()) {
			throw new RecordNotFoundException("there is no User with name "+name);
		}
		return user.get();
	}
	

	public User register(RegisterRequest request) {
//		Set<Role> role = new HashSet<>();
//		ResponseEntity<Role> role1 = null;
		RoleEnum role = RoleEnum.USER;
		if(request.getType().equals(AccountType.COMPANY)) {			
		role = RoleEnum.ADMIN;
		}
//		role.add(role1.getBody());
		Optional<User> userr = userRepository.findByUsername(request.getUsername());
		if(userr.isPresent()) {
			throw new RecordIsAlreadyExist("This User Name Is Already Uses Please Take Another One ");
		}
		
		User user = User.builder()
				.phone(request.getPhone())
				.username(request.getUsername())
				.address(request.getAddress())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.role(role)
				.accountType(request.getType())
				.longitude(request.getLongitude())
				.latitude(request.getLatitude())
				.balance(0.0)
				.latitude(0.0)
				.longitude(0.0)
				.rate(0.0)
				.build();
		userRepository.save(user);
		return user;
	}
	
	public AuthenticationResponse genToken(User user) {
		var jwtToken = jwtService.generateToken(user);
		
		return AuthenticationResponse.builder()
				.token(jwtToken)
				.build();
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		var user = userRepository.findByUsername(request.getUsername()).orElseThrow();
var jwtToken = jwtService.generateToken(user);
		return AuthenticationResponse.builder().token(jwtToken).build();
	
	}
	
	

	public Optional<User> findUserByEmail(String email) {
		
		return userRepository.findByEmail(email);
	}

	public void save(User userr) {
		userRepository.save(userr);
		
	}

	public boolean checkUserName(String username) {
		// TODO Auto-generated method stub
		return userRepository.existsByUsername(username);
	}

//	public List<UserDto> getByUserName(String username) {
//		List<User> users = userRepository.findAllByUsernameContaining(username);
//		if(users.isEmpty()) {
//			throw new RecordNotFoundException("there is no user with name "+username+" or maybe is already worker for another company");
//		}
//		List<UserDto> usersDto = new ArrayList<>();
//		for(User i : users) {
//			
//		UserDto appUserDto = userMapper.mapToDto(i);
//		usersDto.add(appUserDto);
//		}
//		return usersDto;
//	}

	public AuthenticationResponse refreshToken(String token) {
		User user = userRepository.findByUsername(authenticationFilter.userName).get();
		 // validate the input token
	    if (!jwtService.isTokenValid(token,user)) {
	       // throw new InvalidTokenException("Invalid refresh token");
	    }
	    
	    
	    // generate a new authentication token
	    String accessToken = jwtService.generateToken(user);
	    
	    // create a new authentication response
	    AuthenticationResponse response = new AuthenticationResponse(accessToken);
	    
	    // return the response
	    return response;
	}

	public List<UserDto> getAllUsersContaining(String search) {
		List<User> users = userRepository.findAllByUsernameContainingAndRoles(search);
		if(users.isEmpty()) {
			throw new RecordNotFoundException("there is no user with name : "+search);
		}
		List<UserDto> usersDto = new ArrayList<>();
		for(User i :users) {
			UserDto userDto = userMapper.mapToDto(i);
			usersDto.add(userDto);
		}
		return usersDto;
	}


	
	
	public User getUser(){
		return findByUserName(authenticationFilter.userName);
		
	}

	public void updateLocation(Double latitude, Double longitude) {
		User user = getUser();
		user.setLatitude(latitude);
		user.setLongitude(longitude);
		userRepository.save(user);
	}
	
	public UserDto mapToDto(User user) {
		return userMapper.mapToDto(user);
	}
	
	public List<UserDto> mapListToDto(List<User> users){
		List<UserDto> usersDto = new ArrayList<>();
		for(User i : users) {
			usersDto.add(mapToDto(i));
		}
		return usersDto;
	}

	public Page<User> findByUserNameContaining(String search, Pageable pageable) {
		Page<User> users = userRepository.findAllByUsernameContaining(search, pageable);
		return users;
	}
}
