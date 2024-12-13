package com.example.meta.store.werehouse.Services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordIsAlreadyExist;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Dto.UserDto;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Invetation;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Enums.Nature;
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.SearchCategory;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Enums.Type;
import com.example.meta.store.werehouse.Mappers.ClientCompanyRMapper;
import com.example.meta.store.werehouse.Mappers.CompanyMapper;
import com.example.meta.store.werehouse.Repositories.ClientProviderRelationRepository;
import com.example.meta.store.werehouse.Repositories.CompanyRepository;
import com.example.meta.store.werehouse.Repositories.InvetationRepository;
import com.example.meta.store.werehouse.Repositories.InvoiceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class ClientService extends BaseService<Company, Long>{
			
	
	private final InvetationRepository invetationClientProviderRepository;
	
	private final ClientProviderRelationRepository clientCompanyRRepository;

	private final ObjectMapper objectMapper;
	
	private final InvoiceRepository invoiceRepository;
	
	private final CompanyRepository companyRepository;
	
	private final CompanyMapper companyMapper;
	
	private final ClientCompanyRMapper clientCompanyMapper;
	
	private final ImageService imageService;
	
	private final ClientProviderRelationRepository companycRepository;
	
	private final CompanyService  companyService;
	
	private final UserService userService;

    DecimalFormat df = new DecimalFormat("#.###");
    
	private final Logger logger = LoggerFactory.getLogger(ClientService.class);
	
	
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	public ResponseEntity<String> deleteClientByIdAndCompanyId(Long id, Company company) {
		ClientProviderRelation clientCompany = clientCompanyRRepository.findById(id)
				.orElseThrow(() ->new RecordNotFoundException("Client Not Found "));
		if(clientCompany.getClient() != null && clientCompany.getClient().getId() == company.getId()) {
			throw new RecordNotFoundException("you can not delete yourself");
		}
		if(clientCompany.getMvt() != 0) {
			clientCompany.setDeleted(true);
			return ResponseEntity.ok("delete successful");
		}
		clientCompanyRRepository.deleteById(id);
		    
		if(clientCompany.getPerson() == null) {
		if(clientCompany.getClient().isVirtual()) {
			super.deleteById(clientCompany.getClient().getId());
		}else
		invetationClientProviderRepository.deleteByCompanyReciverIdAndCompanySenderId(clientCompany.getClient().getId(), company.getId());
		}else {	
			clientCompanyRRepository.deleteById(id);
			invetationClientProviderRepository.deleteByClientIdAndCompanyReciverIdOrCompanySenderId(clientCompany.getPerson().getId(), company.getId(), company.getId());
		}
		return ResponseEntity.ok("delete successful");
		
	}

	
	public void paymentInpact(Long clientId, Long companyId, Double amount, Invoice invoice) {
		ClientProviderRelation client = clientCompanyRRepository.findByClientIdAndProviderId(clientId, companyId).orElseThrow(() -> new RecordNotFoundException("you are not his client"));
		Double rest;
		if(invoice.getRest() == 0) {
			rest = round(invoice.getPrix_invoice_tot()-amount-client.getAdvance());
		}else {
			rest = round(invoice.getRest()-amount);
		}
		if(rest > 0) {
			invoice.setRest(rest);
			client.setCredit(round(client.getCredit() - amount -client.getAdvance()));
			client.setAdvance(0.0);
			invoice.setPaid(PaymentStatus.INCOMPLETE);
			logger.warn("rest is more than 0"+ invoice.getPaid());
		}
		else {
			invoice.setRest(0.0);
			invoice.setPaid(PaymentStatus.PAID);
			client.setAdvance(round(client.getAdvance() - rest));
			client.setCredit(0.0);
		}
		invoiceRepository.save(invoice);			
	}
	

	public List<ClientProviderRelationDto> getAllMyClient(Company company, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);;
		Page<ClientProviderRelation> clients = clientCompanyRRepository.getAllByProviderIdAndIsDeletedFalse(company.getId(), pageable);
		if(clients.isEmpty()) {
			throw new RecordNotFoundException("There Is No Client Yet");
		}
		List<ClientProviderRelationDto> clientsDto = new ArrayList<>();
		for(ClientProviderRelation i : clients) {
			ClientProviderRelationDto clientDto = clientCompanyMapper.mapToDto(i);
			if(i.getPerson() != null) {
			logger.warn(i.getPerson().getImage()+" image user");
			logger.warn(clientDto.getPerson().getImage()+" image user dto");
			}
			clientsDto.add(clientDto);
		}
		logger.warn("size client dto " +clientsDto.size());
		return clientsDto;
	}
	

	public ResponseEntity<ClientProviderRelationDto> insertClient(String company, MultipartFile file, Company myCompany)
			throws JsonMappingException, JsonProcessingException{

		CompanyDto companyDto = objectMapper.readValue(company, CompanyDto.class);
		boolean existName = companyRepository.existsByName(companyDto.getName());
		if (existName) {
			throw new RecordIsAlreadyExist("This Name Is Already Exist Please Choose Another One");
		}
		boolean existCode = companyRepository.existsByCode(companyDto.getCode());
		if (existCode) {
			throw new RecordIsAlreadyExist("This code Is Already Exist Please Choose Another One");
		}
		Company company1 = companyMapper.mapToEntity(companyDto);
		company1.setVirtual(true);
		company1.setIsVisible(PrivacySetting.ONLY_ME);
		if (file != null) {
			String newFileName = imageService.insertImag(file, myCompany.getUser().getId(), "company");
			company1.setLogo(newFileName);
		}
		companyRepository.save(company1);
		ClientProviderRelation relation = new ClientProviderRelation();
		relation.setAdvance(0.0);
		relation.setMvt(0.0);
		relation.setCredit(0.0);
		relation.setProvider(myCompany);
		relation.setClient(company1);
		companycRepository.save(relation);
		ClientProviderRelationDto dto = clientCompanyMapper.mapToDto(relation);
		return ResponseEntity.ok(dto);
	}
	
	

	public ResponseEntity<CompanyDto> updateClient(String companyDto, MultipartFile file, Company myCompany) throws JsonMappingException, JsonProcessingException {
		return companyService.upDateCompany(companyDto, file, myCompany);
	}

	public ResponseEntity<CompanyDto> updateClientt(String companyDto, Company myCompany) throws JsonMappingException, JsonProcessingException {
		return companyService.upDateCompany(companyDto, null, myCompany);
	}
	
	

	public List<UserDto> findAllMyByNameContainingOrCodeContainingAndPersonId(String search, Long companyId) {
		List<User> users = companycRepository.findByMyUserContaining(search, companyId);
		if(users.isEmpty()) {
			throw new RecordNotFoundException("there is no user as client with name :"+search);
		}
		List<UserDto> usersDto = new ArrayList<>();
		for(User i : users) {
			UserDto userDto = userService.mapToDto(i);
			usersDto.add(userDto);
		}
		return usersDto;
	}
		

	public List<ClientProviderRelationDto> getAllMyContaining(String search,Long id, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<ClientProviderRelation> clientProviderRelation = clientCompanyRRepository.findByMyCompanyAndUserContaining(search, id, pageable);
		Page<ClientProviderRelation> peronProviderRelation = clientCompanyRRepository.findByMyCompanyAndClientContaining(search, id, pageable);
		List<ClientProviderRelation> response = new ArrayList<>();
		response.addAll(clientProviderRelation.getContent());
		response.addAll(peronProviderRelation.getContent());
		return mapToDto(response);

	}
	
	
	
	public void acceptedInvetation(Invetation invetation) {
		ClientProviderRelation relation = new ClientProviderRelation();
		boolean existRelation = false;
		switch (invetation.getType()) {
		case COMPANY_SEND_CLIENT_COMPANY: {
			Optional<ClientProviderRelation> r = clientCompanyRRepository.findByClientIdAndProviderId(invetation.getCompanySender().getId(),invetation.getCompanyReciver().getId());
			if(r.isPresent()) {
				existRelation = true;
			}
			relation.setProvider(invetation.getCompanyReciver());
			relation.setClient(invetation.getCompanySender());
			break;
		}
		case USER_SEND_CLIENT_COMPANY: {
			Optional<ClientProviderRelation> r = clientCompanyRRepository.findByProviderIdAndPersonId(invetation.getCompanyReciver().getId(),invetation.getClient().getId());
			if(r.isPresent()) {
				existRelation = true;
			}
			relation.setProvider(invetation.getCompanyReciver());
			relation.setPerson(invetation.getClient());
			break;
		}
			
		case COMPANY_SEND_PROVIDER_COMPANY: {
			Optional<ClientProviderRelation> r = clientCompanyRRepository.findByClientIdAndProviderId(invetation.getCompanyReciver().getId(),invetation.getCompanySender().getId());
			if(r.isPresent()) {
				existRelation = true;
			}
			relation.setProvider(invetation.getCompanySender());
			relation.setClient(invetation.getCompanyReciver());
			break;
		}
		case COMPANY_SEND_PROVIDER_USER: {
			Optional<ClientProviderRelation> r = clientCompanyRRepository.findByProviderIdAndPersonId(invetation.getCompanySender().getId(),invetation.getClient().getId());
			if(r.isPresent()) {
				existRelation = true;
			}
			relation.setProvider(invetation.getCompanySender());
			relation.setPerson(invetation.getClient());
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + invetation.getType());
		}
//		if(invetation.getType() == Type.CLIENT) {
//			if(invetation.getClient() != null) {
//				if(invetation.getCompanySender() != null) {
//					Optional<ClientProviderRelation> r = clientCompanyRRepository.findByProviderIdAndPersonId(invetation.getCompanySender().getId(),invetation.getClient().getId());
//					if(r.isPresent()) {
//						existRelation = true;
//					}
//					relation.setProvider(invetation.getCompanySender());
//					}else {
//						Optional<ClientProviderRelation> r = clientCompanyRRepository.findByProviderIdAndPersonId(invetation.getCompanyReciver().getId(),invetation.getClient().getId());
//					if(r.isPresent()) {
//						existRelation = true;
//					}
//					relation.setProvider(invetation.getCompanyReciver());
//					}
//					relation.setPerson(invetation.getClient());
//			}else {
//				Optional<ClientProviderRelation> r = clientCompanyRRepository.findByProviderIdAndPersonId(invetation.getCompanySender().getId(),invetation.getCompanyReciver().getId());
//				if(r.isPresent()) {
//					existRelation = true;
//				}
//				relation.setProvider(invetation.getCompanySender());
//				relation.setClient(invetation.getCompanyReciver());
//			}
//			
//		}
//		else {
//			Optional<ClientProviderRelation> r = clientCompanyRRepository.findByProviderIdAndPersonId(invetation.getCompanySender().getId(),invetation.getCompanyReciver().getId());
//			if(r.isPresent()) {
//				existRelation = true;
//			}
//			relation.setProvider(invetation.getCompanyReciver());
//			relation.setClient(invetation.getCompanySender());
//		}
		if(!existRelation) {			
		relation.setCredit(0.0);
		relation.setAdvance(0.0);
		relation.setMvt(0.0);
		}else {			
			relation.setCredit(relation.getCredit());
			relation.setAdvance(relation.getAdvance());
			relation.setMvt(relation.getMvt());	
		}
		relation.setDeleted(false);
		clientCompanyRRepository.save(relation);
		
	}
	

	
	private double round(double value) {
		return Math.round(value * 100.0) / 100.0; // Round to two decimal places
	}
	
	private ClientProviderRelationDto checkIfHasRelation(Long myCompanyId,Long userId) {
		if(myCompanyId != 0) {
			Optional<ClientProviderRelation> relation = companycRepository.findByProviderIdAndPersonId(myCompanyId, userId);
			if(relation.isPresent()) {
				ClientProviderRelationDto relationDto = clientCompanyMapper.mapToDto(relation.get());
				return relationDto;
			}
			
		}
	return null;
	}


	public List<UserDto> getAllPersonContaining(String search,int page, int pageSize) {

		Pageable pageable = PageRequest.of(page, pageSize);
		Page<User> users = userService.findByUserNameContaining(search, pageable);
		List<UserDto> response = userService.mapListToDto(users.getContent());
		return response;
	}
	

	
	private List<ClientProviderRelationDto> mapToDto(List<ClientProviderRelation> response){
		List<ClientProviderRelationDto> clientProviderDto = new ArrayList<>();
		for(ClientProviderRelation i : response) {
			ClientProviderRelationDto dto = clientCompanyMapper.mapToDto(i);
			clientProviderDto.add(dto);
			logger.warn(clientProviderDto.size()+" size of return client provider relation "+i.getId());
		}
		return clientProviderDto;
	}


	public List<UserDto> getAllMyPersonContaining(String search, Long id, int page, int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<User> user = clientCompanyRRepository.findByUserNameContaining(id, search, pageable);
		List<UserDto> userDto = userService.mapListToDto(user.getContent());
		return userDto;
	}
	
	
	
}
