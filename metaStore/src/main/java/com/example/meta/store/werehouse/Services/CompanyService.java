package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordIsAlreadyExist;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.RegisterRequest;
import com.example.meta.store.Base.Security.Entity.Role;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.RoleService;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.CompanyController;
import com.example.meta.store.werehouse.Dtos.CompanyDto;
import com.example.meta.store.werehouse.Entities.Category;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.InvoiceType;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Mappers.CompanyMapper;
import com.example.meta.store.werehouse.Repositories.ClientProviderRelationRepository;
import com.example.meta.store.werehouse.Repositories.CompanyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService extends BaseService<Company, Long> {

	private final CompanyRepository companyRepository;

	private final ClientProviderRelationRepository clientRRepository;
	private final CompanyMapper companyMapper;

	private final RoleService roleService;

	private final UserService userService;

	private final ImageService imageService;
	
	private final CategoryService categoryService;
	
	private final SubCategoryService subCategoryService;
	
	private final WorkerService workerService;
	
	private final ObjectMapper objectMapper;


	private final Logger logger = LoggerFactory.getLogger(CompanyController.class);
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	
	
	public ResponseEntity<CompanyDto> insertCompany(String company, MultipartFile file, User user)
			throws JsonMappingException, JsonProcessingException {
		existsByUser(user.getId());
		CompanyDto companyDto = objectMapper.readValue(company, CompanyDto.class);
		 existsByName(companyDto.getName());
		existByCode(companyDto.getCode());
		Company company1 = companyMapper.mapToEntity(companyDto);
		company1.setUser(user);
		company1.setRaters(0);
		company1.setBalance(0.0);
		company1.setRate((long) 0);
		company1.setVirtual(false);
		company1.setLongitude(0.0);
		company1.setLatitude(0.0);
		if (file != null) {
			String newFileName = imageService.insertImag(file, user.getId(), "company");
			company1.setLogo(newFileName);
		}
		Set<Role> role = new HashSet<>();
		ResponseEntity<Role> role2 = roleService.getById((long) 1);
		role.add(role2.getBody());
		role.addAll(user.getRoles());
		user.setRoles(role);
		userService.save(user);
		companyRepository.save(company1);
		ClientProviderRelation relation = new ClientProviderRelation();
		relation.setAdvance(0.0);
		relation.setMvt(0.0);
		relation.setCredit(0.0);
		relation.setProvider(company1);
		relation.setClient(company1);
		clientRRepository.save(relation);
		Category category =   categoryService.addDefaultCategory(company1);
		subCategoryService.addDefaultSubCategory(company1, category);
		return new ResponseEntity<CompanyDto>(HttpStatus.ACCEPTED);
	}
	
	public ResponseEntity<Company> insertCompanyWithoutUser(RegisterRequest request, User user){
		Company company = new Company();
		logger.warn(request.getCategory()+" : company category");
		existsByName(request.getUsername());
		company.setName(request.getUsername());
		company.setEmail(request.getEmail());
		company.setCategory(request.getCategory());
		company.setPhone(request.getPhone());
		company.setAddress(request.getAddress());
		company.setIsVisible(PrivacySetting.PUBLIC);
		company.setBalance(0.0);
		company.setUser(user);
		company.setIsPointsSeller(false);
		company.setLongitude(0.0);
		company.setLatitude(0.0);
		company.setInvoiceType(InvoiceType.NOT_SAVED);
		companyRepository.save(company);
		ClientProviderRelation relation = new ClientProviderRelation();
		relation.setAdvance(0.0);
		relation.setMvt(0.0);
		relation.setCredit(0.0);
		relation.setProvider(company);
		relation.setClient(company);
		clientRRepository.save(relation);
		Category category =   categoryService.addDefaultCategory(company);
		subCategoryService.addDefaultSubCategory(company, category);
		return new ResponseEntity<Company>(HttpStatus.ACCEPTED);		
	}
	
	public void existsByUser(Long userId) {
		Boolean exist = companyRepository.existsByUserId(userId);
		 if (exist) {
				throw new RecordIsAlreadyExist("You Already have A Company");
			}
	}
	
	public void  existsByName(String companyName) {
		Boolean exist = companyRepository.existsByName(companyName);
		if (exist) {
			throw new RecordIsAlreadyExist("This Name Is Already Exist Please Choose Another One");
		}
	}
	
	public void existByCode(String code) {
		boolean existCode = companyRepository.existsByCode(code);
		if (existCode) {
			throw new RecordIsAlreadyExist("This code Is Already Exist Please Choose Another One");
		}
	}
	
	public ResponseEntity<CompanyDto> upDateCompany(String companyDto, MultipartFile file)
			throws JsonMappingException, JsonProcessingException {
		CompanyDto companyDto1 = objectMapper.readValue(companyDto, CompanyDto.class);
		logger.warn(companyDto1.getId()+" company id");;
		Company company = companyRepository.findById(companyDto1.getId()).orElseThrow(() -> new RecordNotFoundException("you don't have a company"));
		if(!company.getName().equals(companyDto1.getName()))
		{
			boolean existName = companyRepository.existsByName(companyDto1.getName());
			if(existName) {				
				throw new RecordIsAlreadyExist("This Name Is Already Exist Please Choose Another One");
			}
		}
		
		if(company.getCode() == null || !company.getCode().equals(companyDto1.getCode()) ) {
			boolean existCode = companyRepository.existsByCode(companyDto1.getCode());
			if(existCode) {				
				throw new RecordIsAlreadyExist("this code is already exist please choose another one");
			}
		}
		if(company.isVirtual()) {
			
		if(company.getMatfisc() == null || !company.getMatfisc().equals(companyDto1.getMatfisc())) {
			boolean existMatfisc = companyRepository.existsByMatfisc(companyDto1.getMatfisc());
			if(existMatfisc) {				
				throw new RecordIsAlreadyExist("this matricule fiscale is already related by another company");
			}
		}
		if(company.getBankaccountnumber() == null || !company.getBankaccountnumber().equals(companyDto1.getBankaccountnumber())) {
			boolean existBanckAccount = companyRepository.existsByBankaccountnumber(companyDto1.getBankaccountnumber());
			if(existBanckAccount) {
				throw new RecordIsAlreadyExist("this banck account is already related by another company ");
			}
		}
		}
		Company updatedCompany = companyMapper.mapToEntity(companyDto1);
		updatedCompany.setParentCompany(company.getParentCompany());
		updatedCompany.setUser(company.getUser());
		if (file != null) {
			String newFileName = imageService.insertImag(file, company.getUser().getId(), "company");
			updatedCompany.setLogo(newFileName);
		}
		else {			
			updatedCompany.setLogo(company.getLogo());
		}
		updatedCompany.setVirtual(company.isVirtual());
		updatedCompany.setLatitude(company.getLatitude());
		updatedCompany.setLongitude(company.getLongitude());
		company = updatedCompany;
		companyRepository.save(company);
		return ResponseEntity.ok(companyDto1);
		
		
	}
	
	public Company findByUserId(Long userId) {
		Optional<Company> company = companyRepository.findByUserId(userId);
		if (company.isEmpty()) {
			return null;
		}
		return company.get();
	}
	
	public Optional<Company> findCompanyIdByUserId(Long userId) {
		Optional<Company> company = companyRepository.findByUserId(userId);
		return company;
	}
	
	public List<CompanyDto> getCompanyContaining(String branshe, Long id) {
		List<Company> companies = companyRepository.findByNameContaining(branshe,id);
		if(companies.isEmpty()) {
			throw new RecordNotFoundException("there is no company with name containing: "+branshe);
		}
		List<CompanyDto> companiesDto = new ArrayList<>();
		for(Company i : companies) {
			CompanyDto companyDto = companyMapper.mapToDto(i);
			companiesDto.add(companyDto);
		}
		return companiesDto;
	}
	
	public ResponseEntity<CompanyDto> getCompanyById(Long id) {
		Optional<Company> company = companyRepository.findById(id);
		if (company.isEmpty()) {
			throw new RecordNotFoundException("you do not have a company");
		}
		CompanyDto companyDto = companyMapper.mapToDto(company.get());
		return ResponseEntity.ok(companyDto);
	}
	
	public CompanyDto getMe(Company company, Long id) {
		Company companyReturnd = company;
//		if(!company.getId().equals(id)) {
//			Company branshe = companyRepository.findById(id)
//					.orElseThrow(() -> new RecordNotFoundException("there is no company with id: "+id));
//			companyReturnd = branshe;
//		}
		CompanyDto companyDto = companyMapper.mapToDto(companyReturnd);
		logger.warn("return c bon company");
		return companyDto;
	}
	
	
	
	public List<CompanyDto> getAllCompany() {
		List<Company> companies = super.getAll();
		if(!companies.isEmpty() && companies != null && !companies.equals(null)) {
			List<CompanyDto> companysDto = new ArrayList<>();
			for(Company i :companies) {
				CompanyDto companyDto = companyMapper.mapToDto(i);
				companysDto.add(companyDto);
			}
			return companysDto;
		}
		throw new RecordNotFoundException("There Is No Company");
	}

	public void acceptedInvetation(Company companySender, Company companyReciver) {
		Set<Company> companies = companyReciver.getBranches();
		companies.add(companySender);
		companyReciver.setBranches(companies);
		companySender.setParentCompany(companyReciver);
		
	}

	public List<CompanyDto> getBranches(Company company) {
		List<CompanyDto> companiesDto = new ArrayList<>();
		if(company.getBranches().size() >0 ) {
			for(Company i : company.getBranches()) {
				CompanyDto companyDto = companyMapper.mapToDto(i);
				companiesDto.add(companyDto);
			}
		}
		return companiesDto;
	}

	public CompanyDto getMyParent(Company company) {
		CompanyDto companyDto = companyMapper.mapToDto(company.getParentCompany());
		return companyDto;
	}


	public List<CompanyDto> getAllCompaniesContainig(User user ,Company company, String search) {
		List<Company> companies = new ArrayList<>();
		if(company == null) {			
			companies = companyRepository.getAllCompaniesContaining(user.getId(),null, search);
			
		}else {			
			companies = companyRepository.getAllCompaniesContaining(user.getId(),company.getId(),search);
		}
		if(companies.isEmpty()) {	
			throw new RecordNotFoundException("there is no company contain : "+search);
		}
		List<CompanyDto> companiesDto = new ArrayList<>();
		for(Company i : companies) {
			CompanyDto companyDto = companyMapper.mapToDto(i);
			companiesDto.add(companyDto);
		}
		return companiesDto;
	}

	@PreAuthorize("hasRole('ROLE_COMPANY')")
	public Company getCompany() {
		User user = userService.getUser();
		Company company = findCompanyIdByUserId(user.getId()).orElseThrow(() -> new RecordNotFoundException("you dont have a company"));
			return company;
		
//		Long companyId = workerService.getCompanyIdByUserName(user.getUsername());
//		if(companyId != null) {			
//		ResponseEntity<Company> company2 = getById(companyId);
//		return Optional.of(company2.getBody());
//		}
//		return Optional.empty();
	}

	public void makeCompanyAsPointSeller(Long companyId, Boolean status) {
		Company company = this.getById(companyId).getBody();
		company.setIsPointsSeller(status);
		
	}

	public void updateLocation(Double latitude, Double longitude) {
		Company company = getCompany();
		company.setLatitude(latitude);
		company.setLongitude(longitude);
		companyRepository.save(company);
	}


}
