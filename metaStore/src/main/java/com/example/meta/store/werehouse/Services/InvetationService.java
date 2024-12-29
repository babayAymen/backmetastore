package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.Role;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.RoleService;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.InvetationDto;
import com.example.meta.store.werehouse.Dtos.WorkerDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Invetation;
import com.example.meta.store.werehouse.Entities.Worker;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Enums.Type;
import com.example.meta.store.werehouse.Mappers.InvetationClientProviderMapper;
import com.example.meta.store.werehouse.Repositories.InvetationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InvetationService extends BaseService<Invetation, Long> {
	
	private final InvetationRepository invetationClientProviderRepository;
	
	private final InvetationClientProviderMapper invetationClientProviderMapper;
	
	private final RoleService roleService;

	private final UserService userService;
	
	private final ClientService clientService;
	
	private final WorkerService workerService;

	private final CompanyService companyService;

	private final Logger logger = LoggerFactory.getLogger(InvetationService.class);
	

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	
	public void requestResponse(Long id, Status status) {
		Invetation invetation = invetationClientProviderRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no request with id "+id));
		switch(status) {
		case ACCEPTED :
			switch (invetation.getType()){ 
			case COMPANY_SEND_WORKER_USER :
			case USER_SEND_WORKER_COMPANY :	
				WorkerDto workerDto = invetationClientProviderMapper.mapInvetationToWorker(invetation);
				invetation.getClient().setRole(RoleEnum.WORKER);
				userService.save(invetation.getClient());
				workerService.insertWorker(workerDto, invetation.getCompanySender());
				break;
			case COMPANY_SEND_PARENT_COMPANY:
				companyService.acceptedInvetation(invetation.getCompanySender(),invetation.getCompanyReceiver());
				break;
			case COMPANY_SEND_PROVIDER_COMPANY:
			case COMPANY_SEND_PROVIDER_USER:
			case COMPANY_SEND_CLIENT_COMPANY:
			case USER_SEND_CLIENT_COMPANY:
				clientService.acceptedInvetation(invetation);					
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + invetation.getType());
			}
			invetation.setStatus(Status.ACCEPTED);
		break;
		case REFUSED:
			logger.warn("in refused case");
			invetation.setStatus(Status.REFUSED);
			break;
		case CANCELLED :
			logger.warn("in canceled case");
			invetation.setStatus(Status.CANCELLED);	
			break;
		case INWAITING :
			break;
		}
		
		invetationClientProviderRepository.save(invetation);		
	}

	public Page<InvetationDto> getInvetation(Long companyId, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<Invetation> invetations =	invetationClientProviderRepository.findAllByClientIdOrCompanyIdOrUserId(companyId, pageable);	
		List<InvetationDto> invetationsDto = new ArrayList<>();
		for(Invetation i : invetations) {
			InvetationDto dto =  invetationClientProviderMapper.mapToDto(i);
			invetationsDto.add(dto);
		}
		logger.warn("response "+invetationsDto.get(0));
		return new PageImpl<>(invetationsDto, pageable, invetations.getTotalElements());
	}
	
	public Page<InvetationDto> getInvitationAsUser(Long clientId, int page , int pageSize){
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<Invetation> invetations =	invetationClientProviderRepository.findAllByClientId(clientId, pageable);	
		List<InvetationDto> invetationsDto = new ArrayList<>();
		for(Invetation i : invetations) {
			InvetationDto dto =  invetationClientProviderMapper.mapToDto(i);
			invetationsDto.add(dto);
		}
		return new PageImpl<>(invetationsDto, pageable, invetations.getTotalElements());
	}

	
	public void cancelRequestOrDeleteFriend(Long clientId, Company provider, Long id) {
		Invetation invetation = super.getById(id).getBody();
		if(invetation == null) {
			throw new RecordNotFoundException("there is no invetation ");
		}
		if(invetation.getCompanySender() != provider && invetation.getClient().getId() != clientId) {
			throw new NotPermissonException("you dont have permission to do that ");
		}
		if(invetation.getStatus() == Status.INWAITING) {
			invetationClientProviderRepository.delete(invetation);
			return;
		}
	}

	public void addInvitation(Long id, Type type, Company company, User user) {
		Invetation invetation = new Invetation();
		invetation.setStatus(Status.INWAITING);
		invetation.setType(type);
		switch (type) {
		case USER_SEND_CLIENT_COMPANY: {
			ResponseEntity<Company> provider = companyService.getById(id);
			if(provider.getBody() != null) {
			invetation.setCompanyReceiver(provider.getBody());
			invetation.setClient(user);
			}
			
			break;
		}
		case COMPANY_SEND_CLIENT_COMPANY: {
			ResponseEntity<Company> provider = companyService.getById(id);
			if(provider.getBody() != null) {
			invetation.setCompanyReceiver(provider.getBody());
			invetation.setCompanySender(company);
			}
			
			break;	
				}
		case USER_SEND_WORKER_COMPANY: {
			ResponseEntity<Company> provider = companyService.getById(id);
			if(provider.getBody() != null) {
			invetation.setCompanyReceiver(provider.getBody());
			invetation.setClient(user);
			}
			
			break;
		}
		case COMPANY_SEND_PROVIDER_USER: {
			Optional<User> client = userService.findById(id);
			if(client.isPresent()) {
			invetation.setCompanySender(company);
			invetation.setClient(client.get());
			}	
			break;
		}
		case COMPANY_SEND_WORKER_USER: {
			Optional<User> client = userService.findById(id);
			if(client.isPresent()) {
			invetation.setCompanySender(company);
			invetation.setClient(client.get());
			}	
			break;	
				}
		case COMPANY_SEND_PARENT_COMPANY: {
			ResponseEntity<Company> provider = companyService.getById(id);
			if(provider.getBody() != null) {
			invetation.setCompanySender(company);
			invetation.setCompanyReceiver(provider.getBody());
			}	
			break;
		}
		case COMPANY_SEND_PROVIDER_COMPANY: {
			ResponseEntity<Company> provider = companyService.getById(id);
			if(provider.getBody() != null) {
			invetation.setCompanySender(company);
			invetation.setCompanyReceiver(provider.getBody());
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		invetationClientProviderRepository.save(invetation);
		
	}
		

}
