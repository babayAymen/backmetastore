package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordIsAlreadyExist;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.SubCategoryController;
import com.example.meta.store.werehouse.Dtos.SubCategoryDto;
import com.example.meta.store.werehouse.Entities.Category;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.SubCategory;
import com.example.meta.store.werehouse.Mappers.SubCategoryMapper;
import com.example.meta.store.werehouse.Repositories.SubCategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SubCategoryService extends BaseService<SubCategory, Long>{


	private final SubCategoryMapper subCategoryMapper;
	
	private final SubCategoryRepository subCategoryRepository;
	
	private final CategoryService categoryService;
	
	private final ImageService imageService;
	
	private final ObjectMapper objectMapper;

	private final Logger logger = LoggerFactory.getLogger(SubCategoryService.class);
	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	
	public ResponseEntity<List<SubCategoryDto>> getSubCategoryByCompany(Company company) {
		List<SubCategory> subCategorys = getAllByCompanyId(company.getId());
		if(subCategorys.isEmpty()) {
			throw new RecordNotFoundException("there is no subCategory");
		}
		List<SubCategoryDto> subCategorysDto = new ArrayList<>();
		for(SubCategory i : subCategorys) {
			SubCategoryDto subCategoryDto = subCategoryMapper.mapToDto(i);
			subCategorysDto.add(subCategoryDto);
		}
		return ResponseEntity.ok(subCategorysDto);
	}

	private List<SubCategory> getAllByCompanyId(Long companyId) {
		return subCategoryRepository.findAllByCompanyId(companyId);
	}
	
	public ResponseEntity<SubCategoryDto> upDateSubCategory( String dto, Company company, MultipartFile file) throws JsonMappingException, JsonProcessingException {
		SubCategoryDto subCategoryDto = objectMapper.readValue(dto, SubCategoryDto.class);
		SubCategory subCategory = subCategoryRepository.findByIdAndCompanyId(subCategoryDto.getId(),company.getId())
				.orElseThrow(() -> new RecordNotFoundException("SubCategory Not Found"));
		SubCategory categ = subCategoryMapper.mapToEntity(subCategoryDto);
		if(subCategoryDto.getCategory().getId() != subCategory.getCategory().getId()) {
			ResponseEntity<Category> category = categoryService.getById(subCategoryDto.getCategory().getId());
			if(category == null) {
				throw new RecordNotFoundException("this category not found: "+subCategoryDto.getCategory().getLibelle());
			}
			categ.setCategory(category.getBody());
		}
		if(file != null) {
			
			String newFileName = imageService.insertImag(file,company.getUser().getId(), "subcategory");
			categ.setImage(newFileName);
		}
		else {				
			categ.setImage(subCategory.getImage());
		}
		categ.setCompany(company);
		subCategoryRepository.save(categ);
		return ResponseEntity.ok(subCategoryDto);		
	}
	
	public SubCategory getDefaultSubCategory(Company company) {
		Optional<SubCategory> subCategory = subCategoryRepository.findByLibelleAndCompanyId("sub category", company.getId());
		return subCategory.get();
	}

	public ResponseEntity<SubCategoryDto> insertSubCategory(String subCatDto, Company company, MultipartFile file)
			throws JsonMappingException, JsonProcessingException{
		SubCategoryDto subCategoryDto = objectMapper.readValue(subCatDto, SubCategoryDto.class);
		Optional<SubCategory> subCategory1 = getByLibelleAndCompanyId(subCategoryDto.getLibelle(),company.getId());
		if(subCategory1.isPresent())  {
			throw new RecordIsAlreadyExist("is already exist");
		}
		SubCategory subCategory = subCategoryMapper.mapToEntity(subCategoryDto);
		if(file != null) {
			String newFileName = imageService.insertImag(file,company.getUser().getId(), "subcategory");
			subCategory.setImage(newFileName);
		}
		subCategory.setCompany(company);
		super.insert(subCategory);
		return new ResponseEntity<SubCategoryDto>(HttpStatus.ACCEPTED);
	}
	
	public ResponseEntity<SubCategoryDto> getSubCategoryById(String name, Company company) {
		SubCategory subCategory = getByLibelleAndCompanyId(name,company.getId())
				.orElseThrow(() -> new RecordNotFoundException("There Is No SubCategory With Libelle : "+name));
		SubCategoryDto dto = subCategoryMapper.mapToDto(subCategory);
		return ResponseEntity.ok(dto);
		
	}
	
	private Optional<SubCategory> getByLibelleAndCompanyId(String name, Long companyId) {
		return subCategoryRepository.findByLibelleAndCompanyId(name,companyId);
		
	}
	
	public void deleteSubCategoryById(Long id, Company company) {
		Optional<SubCategory> subCategory = getByIdAndCompanyId(id,company.getId());
		if(subCategory.isEmpty()) {
			throw new RecordNotFoundException("This SubCategory with id: "+id+" Does Not Exist");
		}
		super.deleteById(id);
		
	}
	
	private Optional<SubCategory> getByIdAndCompanyId(Long id , Long companyId) {
		return subCategoryRepository.findByIdAndCompanyId(id, companyId);
	}
	
	public List<SubCategoryDto> getAllSubCategoryByCompanyIdAndCategoryId(Long categoryId, Long companyId) {
		
		return getByCompanyIdAndCategoryId(companyId,categoryId);			
	}
	
	private List<SubCategoryDto> getByCompanyIdAndCategoryId(Long id, Long categoryId) {
		List<SubCategory> subCategory = subCategoryRepository.findAllByCompanyIdAndCategoryId(id,categoryId);
		if(subCategory.isEmpty()) {
			throw new RecordNotFoundException("there is no sub category inside this category"+ id +" categ id :"+categoryId);
		}
		List<SubCategoryDto> listSubCategoryDto = new ArrayList<>();
		for(SubCategory i: subCategory) {
			SubCategoryDto subCategoryDto = subCategoryMapper.mapToDto(i);
			listSubCategoryDto.add(subCategoryDto);
		}
		logger.warn("c bon fi sub category "+listSubCategoryDto.size());
		return listSubCategoryDto;
		
	}
	
	public void addDefaultSubCategory(Company company1, Category category) {
		SubCategory subCategory = new SubCategory();
		subCategory.setCategory(category);
		subCategory.setCode("subCode");
		subCategory.setCompany(company1);
		subCategory.setLibelle("sub category");
		subCategoryRepository.save(subCategory);
		
	}
	/////////////////////////////////////////////////////// not work ///////////////////////////////////////////////////
	
	private Optional<SubCategory> getByLibellea(String libelle, Long companyId) {
		return subCategoryRepository.findByLibelleAndCompanyId(libelle, companyId);
	}



}
