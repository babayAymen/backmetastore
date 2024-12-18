package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	
	public Page<SubCategoryDto> getSubCategoryByCompany(Long id, int page , int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Page<SubCategory> subCategorys = subCategoryRepository.findAllByCompanyId(id, pageable);
		List<SubCategoryDto> subCategorysDto = new ArrayList<>();
		for(SubCategory i : subCategorys.getContent()) {
			SubCategoryDto subCategoryDto = subCategoryMapper.mapToDto(i);
			subCategorysDto.add(subCategoryDto);
		}
		logger.warn(id+" sub category size : "+subCategorysDto.size());
		return new PageImpl<>(subCategorysDto, pageable, subCategorys.getTotalElements());
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
		SubCategoryDto response = subCategoryMapper.mapToDto(categ);
		return ResponseEntity.ok(response);		
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
		SubCategoryDto response = subCategoryMapper.mapToDto(subCategory);
		return  ResponseEntity.ok(response);
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
	
	public List<SubCategoryDto> getAllSubCategoryByCompanyIdAndCategoryId(Long categoryId, Long companyId, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<SubCategory> subCategory = subCategoryRepository.findAllByCompanyIdAndCategoryId(companyId,categoryId, pageable);
		List<SubCategoryDto> dtos = mapToSubCategoryDto(subCategory.getContent());
		return dtos;
	}
	
	private List<SubCategoryDto> mapToSubCategoryDto(List<SubCategory> subCategories) {
		List<SubCategoryDto> listSubCategoryDto = new ArrayList<>();
		for(SubCategory i: subCategories) {
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
