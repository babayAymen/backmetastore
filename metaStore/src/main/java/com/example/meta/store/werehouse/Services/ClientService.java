package com.example.meta.store.werehouse.Services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public void deleteClientByIdAndCompanyId(Long id, Company company) {
		ClientProviderRelation clientCompany = clientCompanyRRepository.findByClientIdAndProviderId(id, company.getId())
				.orElseThrow(() ->new RecordNotFoundException("Client Not Found "));
		if(clientCompany.getMvt() != 0) {
			clientCompany.setDeleted(true);
			return;
		} 
		if(clientCompany.getPerson() == null) {		
			clientCompanyRRepository.deleteByClientIdAndProviderId(id, company.getId());
		if(clientCompany.getClient().isVirtual()) {
			super.deleteById(id);
		}
		if(!clientCompany.getClient().isVirtual())
		invetationClientProviderRepository.deleteByCompanyReciverIdAndCompanySenderId(id, company.getId());
		}else {			
			clientCompanyRRepository.deleteByPersonIdAndProviderId(id, company.getId());
			invetationClientProviderRepository.deleteByClientIdAndCompanyReciverIdOrCompanySenderId(id, company.getId(), company.getId());
		}
		
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
	
	
//	public void addExistClient(Long clientId, Type type, Company company) {
//		Invetation invetationClientCompany = new Invetation();
//		logger.warn(type+" type is ");
//		if(type.equals("company")) {
//			logger.warn("yes it is");
//			ResponseEntity<Company> client = companyService.getById(clientId);			
//			if(client.getBody().isVirtual()) {
//				Optional<ClientProviderRelation> clientCompany = companycRepository.findByClientIdAndProviderId(clientId, company.getId());
//				clientCompany.get().setDeleted(false);
//				return;
//			}
//			invetationClientCompany.setCompanyReciver(client.getBody());
//			
//		}
//		else {
//			logger.warn("no in the else ");
//			User client = userService.findById(clientId).get();
//			invetationClientCompany.setClient(client);
//		}
//		invetationClientCompany.setCompanySender(company);
//		invetationClientCompany.setStatus(Status.INWAITING);
//		invetationClientCompany.setType(Type.CLIENT);
//		
//		invetationClientProviderRepository.save(invetationClientCompany);
//	}
//	
	public List<ClientProviderRelationDto> getAllMyClient(Company company) {
		
		List<ClientProviderRelation> clients = clientCompanyRRepository.getAllByProviderIdAndIsDeletedFalse(company.getId());
		if(clients == null) {
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
		return clientsDto;
	}
	

	public void insertClient(String company, MultipartFile file, Company myCompany)
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
	}
	
	

	public void updateClient(String companyDto, MultipartFile file, Company myCompany) throws JsonMappingException, JsonProcessingException {
		companyService.upDateCompany(companyDto, file);
		//the same ompl ofupdate company in company service 
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
		

	public List<ClientProviderRelationDto> getAllMyContaining(String search,Company company, SearchCategory category) {
		logger.warn("id company from service "+company.getId());
		List<ClientProviderRelation> clientProviderRelation = clientCompanyRRepository.findByMyCompanyAndUserContaining(search, company.getId());
		clientProviderRelation.addAll(clientCompanyRRepository.findByMyCompanyAndClientContaining(search, company.getId()));
		if(clientProviderRelation.isEmpty()) {
			throw new RecordNotFoundException("there is no client with name or code contain "+search);
		}
		List<ClientProviderRelationDto> clientProviderDto = new ArrayList<>();
		for(ClientProviderRelation i : clientProviderRelation) {
			ClientProviderRelationDto dto = clientCompanyMapper.mapToDto(i);
			clientProviderDto.add(dto);
			logger.warn(clientProviderDto.size()+" size of return client provider relation "+i.getId());
		}
		return clientProviderDto;

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
	
	/////////////////////////////////////////////////////// not work ///////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////



	
	
	
}
