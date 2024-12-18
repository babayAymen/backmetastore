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
import com.example.meta.store.werehouse.Controllers.ArticleController;
import com.example.meta.store.werehouse.Dtos.CategoryDto;
import com.example.meta.store.werehouse.Entities.Category;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Mappers.CategoryMapper;
import com.example.meta.store.werehouse.Repositories.CategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService extends BaseService<Category, Long>{


	private final CategoryMapper categoryMapper;
	
	private final CategoryRepository categoryRepository;
	
	private final ImageService imageService;
	
	private final ObjectMapper objectMapper;

	private final Logger logger = LoggerFactory.getLogger(CategoryService.class);
	
	
	
	///////////////////////////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////////////////
	public ResponseEntity<CategoryDto> insertCategory( String catDto, Company company, MultipartFile file)
			throws JsonMappingException, JsonProcessingException {
		CategoryDto categoryDto = objectMapper.readValue(catDto, CategoryDto.class);
		Optional<Category> category1 = categoryRepository.findByLibelleAndCompanyId(categoryDto.getLibelle(),company.getId());
		if(category1.isPresent())  {
			throw new RecordIsAlreadyExist("Category "+categoryDto.getLibelle()+"is already exist");
		}
		Category category = categoryMapper.mapToEntity(categoryDto);
		if(file !=null) {

			String newFileName = imageService.insertImag(file,company.getUser().getId(), "category");
			category.setImage(newFileName);
		}
		category.setCompany(company);
		super.insert(category);
		CategoryDto response = categoryMapper.mapToDto(category);
		return  ResponseEntity.ok(response);
	}
	
	public CategoryDto getByLibelleAndCompanyId(Company company, String name) {
		Optional<Category> category = categoryRepository.findByLibelleAndCompanyId(name,company.getId());
		if(category.isEmpty()) {
			throw new RecordNotFoundException("There Is No Category With Libelle : "+name);
		}
		CategoryDto dto = categoryMapper.mapToDto(category.get());
		return dto;
	}

	public Category getDefaultCategory(Company company) {
		Optional<Category> category = categoryRepository.findByLibelleAndCompanyId("category", company.getId());
		return category.get();
	}
	
	public ResponseEntity<CategoryDto> upDateCategory( String catDto, Company company, MultipartFile file) throws JsonMappingException, JsonProcessingException {
		CategoryDto categoryDto = objectMapper.readValue(catDto, CategoryDto.class);
		Category category = categoryRepository.findByIdAndCompanyId(categoryDto.getId(),company.getId())
				.orElseThrow(() -> new RecordNotFoundException("Category Not Found"));
			Category categ = categoryMapper.mapToEntity(categoryDto);
			if(file != null) {

				String newFileName = imageService.insertImag(file,company.getUser().getId(), "category");
				categ.setImage(newFileName);
			}else {

				categ.setImage(category.getImage());
			}
			categ.setCompany(company);
			categoryRepository.save(categ);
			CategoryDto response = categoryMapper.mapToDto(categ);
			return ResponseEntity.ok(response);
			
	}
	
	public List<CategoryDto> getCategoryByCompany( Long id) {
		List<Category> categorys = getAllByCompanyId(id);
		
		if(categorys.isEmpty()) {
			throw new RecordNotFoundException("there is no category");
		}
		List<CategoryDto> categorysDto = new ArrayList<>();
		for(Category i : categorys) {
			CategoryDto categoryDto = categoryMapper.mapToDto(i);
			categorysDto.add(categoryDto);
		}
		return categorysDto;
	}

	public void deleteCategoryById(Long id, Company company) {
		Optional<Category> category = getByIdAndCompanyId(id,company.getId());
		if(category.isEmpty()) {
			throw new RecordNotFoundException("This Category Does Not Exist");
		}
		super.deleteById(id);		
	}
	
	private List<Category> getAllByCompanyId(Long companyId) {
		logger.warn(companyId+ "company id in get all by company category");
		return categoryRepository.findAllByCompanyId(companyId);
	}
	
	
	private Optional<Category> getByIdAndCompanyId(Long id , Long companyId) {
		return categoryRepository.findByIdAndCompanyId(id, companyId);
	}

	
	
	public Category addDefaultCategory(Company company1) {
		Category category = new Category();
		category.setCode("cat");
		category.setCompany(company1);
		category.setLibelle("category");
		categoryRepository.save(category);
		return category;
		
	}

	public Page<CategoryDto> getCategoriesByPage(int page, int pageSize, Long id) {
	    Sort sort = Sort.by(Sort.Direction.DESC, "id");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Page<Category> categories = categoryRepository.findAllByCompanyId(id,pageable);
		List<CategoryDto> categoriesDto = categories.stream()
				.map(categoryMapper::mapToDto)
				.toList();
		logger.warn("category size " +categories.getSize()+" tot page "+categories.getTotalPages());
		return new PageImpl<>(categoriesDto, pageable, categories.getTotalElements());
		}
	
	
	


}
