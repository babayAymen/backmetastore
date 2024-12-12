package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordIsAlreadyExist;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.ClientProviderRelationDto;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Invetation;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Enums.Type;
import com.example.meta.store.werehouse.Mappers.ClientCompanyRMapper;
import com.example.meta.store.werehouse.Mappers.CompanyMapper;
import com.example.meta.store.werehouse.Repositories.ClientProviderRelationRepository;
import com.example.meta.store.werehouse.Repositories.CompanyRepository;
import com.example.meta.store.werehouse.Repositories.InvetationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProviderService extends BaseService<Company, Long> {
		
	private final InvetationRepository invetationClientProviderRepository;	    
	private final ClientProviderRelationRepository companycRepository;
	private final CompanyRepository			 companyRepository;
	private final ClientCompanyRMapper clientCompanyMapper;
	private final CompanyMapper companyMapper;
	private final ObjectMapper objectMapper;
	private final ImageService imageService;
	private final CompanyService companyService;
	private final Logger logger = LoggerFactory.getLogger(ProviderService.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////

	public List<CompanyDto> getAllProviderContaining(String search, Long myCompanyId, Long personId) {
		List<Company> providers = companyRepository.findAllContaining(search, myCompanyId, personId);
		List<CompanyDto> providersDto = new ArrayList<>();
		logger.warn(providers.size()+" size of providers company as providers");
		for(Company i : providers) {
			logger.warn(i.getId()+" id of providers company as providers");
			logger.warn(i.getProviderCompany().size()+" id of providers company as providers");
			CompanyDto providerDto = companyMapper.mapToDto(i);
			ClientProviderRelationDto exists = checkIfHasRelation(i.getId(), myCompanyId);
				providerDto.setClientcompany(exists);
			providersDto.add(providerDto);
		}
		return providersDto;
	}

	//@Cacheable(value = "provider", key = "#root.methodName")

	//@Cacheable(value = "provider", key = "#root.methodName")


/////////////////////////////////////////////////////// company insted of provider ///////////////////////////////////////////////
	
	public void deleteProviderById(Long id, Company myCompany) {
		ClientProviderRelation providerCompany = companycRepository.findByProviderIdAndClientIdAndIsDeletedFalse(id, myCompany.getId()).orElseThrow(() -> new RecordNotFoundException("this provider is already not yours"));
		if(providerCompany.getMvt() !=0) {
			providerCompany.setDeleted(true);
			return;
		}
		companycRepository.deleteByProviderIdAndClientId(id,myCompany.getId());
		if(providerCompany.getProvider().isVirtual()) {
			super.deleteById(id);
			return;
		}		
		invetationClientProviderRepository.deleteByCompanyReciverIdAndCompanySenderId(id, myCompany.getId());
		
		
	} 

	
	public List<ClientProviderRelationDto> getAllMyProvider(Long companyId, int page , int pageSize) {

		Pageable pageable = PageRequest.of(page, pageSize);
		Page<ClientProviderRelation> providers = companycRepository.findAllByClientIdAndIsDeletedFalse(companyId, pageable);
		List<ClientProviderRelationDto> providersDto = new ArrayList<>();
		for(ClientProviderRelation i : providers) {
			ClientProviderRelationDto providerDto = clientCompanyMapper.mapToDto(i);
			providersDto.add(providerDto);
		}
		logger.warn("c bon je lil provider size : "+providersDto.size());
		return providersDto;
	}
	
	
	
	
	
	public List<CompanyDto> getAllProvidersContaining(Long companyId, String search, Long userId) {
		List<Company> providers = new ArrayList<>();
		if(companyId != 0) {
			 providers = companyRepository.findAllMyByNameContainingOrCodeContainingAndProviderId(search, companyId);
		}else {
			 providers = companycRepository.findAllMyByNameContainingOrCodeContaining(search, userId);
		}
		
		if(providers.isEmpty()) {
			throw new RecordNotFoundException(search);
		}
		List<CompanyDto> companiesDto = new ArrayList<>();
		for(Company i : providers) {
			CompanyDto companyDto = companyMapper.mapToDto(i);
			companiesDto.add(companyDto);
		}
		logger.warn(companiesDto.size()+" size");
		return companiesDto;
	}
	
	
	
	
	
	
	private ClientProviderRelationDto checkIfHasRelation(Long providerId, Long myCompanyId) {
	//	for(ClientProviderRelation i : provider.getProviderCompany())
		Optional<ClientProviderRelation> relation = companycRepository.findByClientIdAndProviderId(myCompanyId, providerId);
			if(relation.isPresent()) {
				
				ClientProviderRelationDto clientCompanyRDto = clientCompanyMapper.mapToDto(relation.get());
				return clientCompanyRDto;	
			}
		//	}
	return null;
	}
	

	
	public List<ClientProviderRelationDto> getAllMyVirtaul(Company company){
		List<ClientProviderRelation> providers = companycRepository.findAllByCompanyIdAndIsVirtualTrue(company.getId());
		if(providers == null) {
			throw new RecordNotFoundException("there is no provider yet");
		}
		List<ClientProviderRelationDto> dtos = new ArrayList<>();
		for(ClientProviderRelation i : providers) {
			ClientProviderRelationDto providerDto = clientCompanyMapper.mapToDto(i);
			dtos.add(providerDto);
		}
		return dtos;
	}
	
	public void insertProvider(String company, MultipartFile file, Company myCompany)
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
		relation.setProvider(company1);
		relation.setClient(myCompany);
		companycRepository.save(relation);
	}
	
	public void updateProvider(String companyDto, MultipartFile file, Company myCompany) throws JsonMappingException, JsonProcessingException {
		companyService.upDateCompany(companyDto, file, myCompany);
		//the same ompl ofupdate company in company service 
	}
	
//	public void addExistProvider(Long id, Company company) {
//		Company provider = companyRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("this copany not found"));
//		if( provider.equals(company) || provider.isVirtual()) {
//			ClientProviderRelation providerCompany = companycRepository.findByProviderIdAndClientIdAndIsDeletedTrue(id, company.getId())
//					.orElseThrow(() -> new RecordNotFoundException("there is no provider"));
//			providerCompany.setDeleted(false);
//			return;
//		}
//		Invetation invetationClientProvider = new Invetation();
//		invetationClientProvider.setCompanySender(company);
//		invetationClientProvider.setCompanyReciver(provider);
//		invetationClientProvider.setStatus(Status.INWAITING);
//		invetationClientProvider.setType(Type.PROVIDER);
//		invetationClientProviderRepository.save(invetationClientProvider);
//	}

	public List<ClientProviderRelationDto> getAllMyProvidersByUserId(String search, Long userId) {
		
		return null;
	}
	

	
	
	
	
	
	
	
}
