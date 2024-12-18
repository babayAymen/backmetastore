package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyDto;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyWithoutTroubleDto;
import com.example.meta.store.werehouse.Dtos.ArticleDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Category;
import com.example.meta.store.werehouse.Entities.CommandLine;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;
import com.example.meta.store.werehouse.Entities.SubArticle;
import com.example.meta.store.werehouse.Entities.SubCategory;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Enums.Unit;
import com.example.meta.store.werehouse.Mappers.ArticleCompanyMapper;
import com.example.meta.store.werehouse.Mappers.ArticleMapper;
import com.example.meta.store.werehouse.Repositories.ArticleCompanyRepository;
import com.example.meta.store.werehouse.Repositories.ArticleRepository;
import com.example.meta.store.werehouse.Repositories.InvoiceRepository;
import com.example.meta.store.werehouse.Repositories.LikeRepository;
import com.example.meta.store.werehouse.Repositories.SubArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService extends BaseService<ArticleCompany, Long>{

	private final ArticleRepository articleRepository;
	
	private final ArticleCompanyRepository articleCompanyRepository;

	private final SubArticleRepository subArticleRepository;
	
	private final ArticleMapper articleMapper; 
	
	private final ArticleCompanyMapper articleCompanyMapper;
	
	private final ObjectMapper objectMapper;

	private final InventoryService inventoryService;

	private final CategoryService categoryService;
	
	private final SubCategoryService subCategoryService;
	
	private final InvoiceRepository invoiceRepository;
	
	private final Logger logger = LoggerFactory.getLogger(ArticleService.class);
	

	/////////////////////////////////////// real work ////////////////////////////////////////////////////////
	public List<ArticleCompanyDto> findRandomArticlesPub( Company myCompany, User user, int offset, int pageSize, CompanyCategory category) {
		Pageable pageable = PageRequest.of(offset, pageSize);
		Page<ArticleCompany> articles;
		Boolean isFav = false;
		if(myCompany == null) {
			articles = articleCompanyRepository.findRandomArticles(user.getId(),user.getLongitude(), user.getLatitude(), category, pageable);
		}
		else {
			articles = articleCompanyRepository.findRandomArticlesPro(myCompany.getId(), myCompany.getLongitude(), myCompany.getLatitude(), category, pageable);	 
		}
			List<ArticleCompanyDto> articlesDto = new ArrayList<>();
			for(ArticleCompany i:articles) {
				logger.warn("id : "+i.getId());
				if(myCompany == null) {
					isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),user.getId());
				}
				else {
					isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),myCompany.getId());
				}
			ArticleCompanyDto dto = articleCompanyMapper.mapToDto(i);
			dto.setIsFav(isFav);
			articlesDto.add(dto);
	}
			logger.warn("category is : "+category);
			logger.warn("size article return "+articlesDto.size()+" article "+articles.getSize());
			return articlesDto;
	}
	
	public List<ArticleCompanyDto> getAllArticleByCompanyId(Long providerId,Long myClientId,Long myCompanyId, int offset, int pageSize) {

		Boolean isFav = false;
		Pageable pageable = PageRequest.of(offset, pageSize);
		Page<ArticleCompany> articles;
		if(providerId == myCompanyId) {
			articles = articleCompanyRepository.findAllByCompanyIdAndIsDeletedFalseOrderByCreatedDateDesc(myCompanyId,pageable);			
		}else {			
		 articles = articleCompanyRepository.findAllByCompanyIdAndIsDeletedFalseOrderByLibelleASC(myCompanyId,myClientId,providerId,pageable);
		}
		if(articles.isEmpty()) {
			throw new RecordNotFoundException("there is no article");
		}
		List<ArticleCompanyDto> articlesDto = new ArrayList<>();
		for(ArticleCompany i : articles) {
			if(myCompanyId == null) {
				isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),myClientId);
			}
			else {
				isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),myCompanyId);
			}
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articleDto.setIsFav(isFav);
			articlesDto.add(articleDto);
		}
		return articlesDto;
	}
	

	//by the article to client 
	public void impactInvoice(List<CommandLine> commandLines) {
		Company client = commandLines.get(0).getInvoice().getClient();
		Category category = categoryService.getDefaultCategory(client);
		SubCategory subCategory = subCategoryService.getDefaultSubCategory(client);
		ArticleCompany article;
		//the code below convey that i add article to client table
		for(int i =0; i < commandLines.size();i++) {
			Optional<ArticleCompany> art = articleCompanyRepository.findByCodeAndProviderId(commandLines.get(i).getArticle().getArticle().getCode(), client.getId());
			logger.warn("quantity of problem ==> "+commandLines.get(i).getQuantity());
//			Double articleCost = round(commandLines.get(i).getArticle().getCost() +
//					(commandLines.get(i).getArticle().getCost()*commandLines.get(i).getArticle().getTva()+
//							commandLines.get(i).getArticle().getCost()*commandLines.get(i).getArticle().getMargin())/100);
//			
			double qte  ;
			if(art.isPresent()) {
				 article = art.get();
				 qte= (commandLines.get(i).getQuantity());
				article.setQuantity(qte+article.getQuantity());
				//do not remove the above line in case of the provider has augmented the article price
				article.setCost(commandLines.get(i).getArticle().getSellingPrice());	
			}else {				
				ArticleCompany ar = commandLines.get(i).getArticle();
				qte = commandLines.get(i).getQuantity();
				 article = new ArticleCompany();
				 article.setUnit(ar.getUnit());
				 article.setMinQuantity(ar.getMinQuantity());
				 article.setQuantity(qte);
				 article.setSellingPrice(0.0);
				 article.setCompany(client);
				 article.setIsVisible(PrivacySetting.ONLY_ME);
				 article.setArticle(commandLines.get(i).getArticle().getArticle());
			article.setCategory(category);
			article.setSubCategory(subCategory);
			article.setCost(ar.getSellingPrice());
			article.setSharedPoint(commandLines.get(i).getArticle().getSharedPoint());
			articleCompanyRepository.save(article);
			}
			inventoryService.impactInvoiceOnClient(client,commandLines.get(i), article);
		}
		
	}
	
	public List<ArticleCompanyDto> getAllProvidersArticleByProviderId( Long id, int offset, int pageSize) {
		List<ArticleCompanyDto> articlesDto = new ArrayList<ArticleCompanyDto>();
		Pageable pageable = PageRequest.of(offset,pageSize);
		Page<ArticleCompany> articles = articleCompanyRepository.findAllByCompanyIdAndIsDeletedFalseOrderByCreatedDateDesc(id,pageable);
		if(articles != null) {
			List<ArticleCompany> articlesContent = articles.getContent();
			for(ArticleCompany i : articlesContent) {
				ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articlesDto.add(articleDto);
			
			}
		}
		logger.warn("articledto size : "+articlesDto.size());
		return articlesDto;
	}
	
	public List<ArticleCompanyDto> getAllArticleByCategoryId(Long categoryId, Long companyId,User user, Company client) {
		List<ArticleCompany> articles;
		if(client != null) {
		if( companyId == client.getId()) {
			articles = articleCompanyRepository.findAllMyByCategoryIdAndCompanyId(categoryId, companyId);
		}else {
			articles = articleCompanyRepository.findAllByCategoryIdAndCompanyId(categoryId, companyId, client.getId());
		}
	}else {
		articles = articleCompanyRepository.findAllByCategoryIdAndPersonId(categoryId, user.getId());
	}

		if(articles == null) {
			throw new RecordNotFoundException("there is no article");
		}
		List<ArticleCompanyDto> articlesDto = new ArrayList<>();
		Boolean isFav = false;
		for(ArticleCompany i : articles) {
			if(client == null) {
				isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),user.getId());
			}
			else {
				isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),client.getId());
			}
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articleDto.setIsFav(isFav);
			articlesDto.add(articleDto);
		}
		logger.warn("article dto size"+articlesDto.size());
		return articlesDto;
	}
	
	public List<ArticleCompanyDto> getAllArticleBySubCategoryIdAndCompanyId(Long subcategoryId, Long companyId,User user, Company client) {
		List<ArticleCompany> articles;
		Boolean isFav = false;
		if(client != null) {
			
		if(companyId == client.getId()) {
			articles = articleCompanyRepository.findAllMyBySubCategoryIdAndCompanyId(subcategoryId, companyId);
		}else {
			articles = articleCompanyRepository.findAllBySubCategoryIdAndCompanyId(subcategoryId, companyId , client.getId());
		}
		}else {			
			articles = articleCompanyRepository.findAllBySubCategoryIdAndPersonId(subcategoryId, companyId ,user.getId());
		}
		if(articles == null) {
			throw new RecordNotFoundException("there is no article");
		}
		List<ArticleCompanyDto> articlesDto = new ArrayList<>();
		for(ArticleCompany i : articles) {
			if(client == null) {
				isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),user.getId());
			}
			else {
				isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),client.getId());
			}
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articleDto.setIsFav(isFav);
			articlesDto.add(articleDto);
		}
		return articlesDto;
	}
	
	
	
	   public ResponseEntity<ArticleCompanyDto> insertArticle(ArticleCompanyDto article, Company provider, Long articleId) {
			Optional<ArticleCompany> existRelation = articleCompanyRepository.findByArticleIdAndCompanyId(articleId, provider.getId());
			if(existRelation.isPresent() && !existRelation.get().getIsDeleted() ) {
				return null;
			}
			ArticleCompany article1 = articleCompanyMapper.mapToEntity(article);
			Article art = findById(articleId);
			article1.setArticle(art);
			if(article1.getProvider() == null || article1.getProvider().getId() == null) {
				article1.setProvider(provider);
			}
			if(article1.getCategory().getId() == null) {
				Category category = categoryService.getDefaultCategory(provider);
				article1.setCategory(category);		
			}
			if(article1.getSubCategory().getId() == null) {
				SubCategory subCategory = subCategoryService.getDefaultSubCategory(provider);
				article1.setSubCategory(subCategory);
			}
			article1.setCommentNumber(0L);
			article1.setLikeNumber(0L);
			if(provider.getIsVisible() == PrivacySetting.ONLY_ME) {	
			article1.setIsVisible(PrivacySetting.ONLY_ME);
			}
			if(provider.getIsVisible() == PrivacySetting.CLIENT && article1.getIsVisible() == PrivacySetting.PUBLIC) {
				article1.setIsVisible(PrivacySetting.CLIENT);
			}
			article1.setCompany(provider);
			article1.setIsDeleted(false);
			if(existRelation.isPresent() && existRelation.get().getIsDeleted()) {
				ArticleCompany relation = existRelation.get();
				existRelation.get().setArticle(art);
				existRelation.get().setProvider(article1.getProvider());
				existRelation.get().setCategory(article1.getCategory());
				existRelation.get().setSubCategory(article1.getSubCategory());
				existRelation.get().setCommentNumber(relation.getCommentNumber());
				existRelation.get().setLikeNumber(relation.getLikeNumber());
				existRelation.get().setIsDeleted(false);
				existRelation.get().setQuantity(article1.getQuantity());
				existRelation.get().setMinQuantity(article1.getMinQuantity());
				existRelation.get().setCompany(relation.getCompany());
				existRelation.get().setIsVisible(article1.getIsVisible());
				existRelation.get().setSellingPrice(article1.getSellingPrice());
				existRelation.get().setUnit(article1.getUnit());
				article1.setId(relation.getId());
				//				articleCompanyRepository.save(relation);
			}else {
			inventoryService.makeInventory(article1, provider);
			articleCompanyRepository.save(article1);
			}
			ArticleCompanyDto response = articleCompanyMapper.mapToDto(article1);
			return ResponseEntity.ok(response);
		}
	
		public void addQuantity(Long id, Double quantity, Company provider) {
			ArticleCompany article = articleCompanyRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no article with id: "+id));
			Double Quantity = round(article.getQuantity()+quantity);
			article.setQuantity(Quantity);
			articleCompanyRepository.save(article);
			inventoryService.addQuantity(article,quantity, provider);

		}
	
		public ResponseEntity<ArticleCompanyDto> upDateArticle( ArticleCompanyDto articleDto, Company provider) {
			ArticleCompany updatedArticle = articleCompanyMapper.mapToEntity(articleDto);
			ArticleCompany article1 =  articleCompanyRepository.findById(articleDto.getId()).orElseThrow(() -> new RecordNotFoundException("there is no article with id: "+articleDto.getId()));	
				 if(!article1.getCompany().equals(provider)) {
					 return null;
				 }
			if(updatedArticle.getProvider() == null) {
				updatedArticle.setProvider(provider);
			}
			if(updatedArticle.getQuantity() != article1.getQuantity()) {
				addQuantity(article1.getId(), updatedArticle.getQuantity()-article1.getQuantity(), provider);
			}
			
			updatedArticle.setCompany(provider);
			if(articleDto.getIsVisible() != article1.getIsVisible()) {
				if(provider.getIsVisible() == PrivacySetting.ONLY_ME) {			
					updatedArticle.setIsVisible(PrivacySetting.ONLY_ME);
					}
				if(provider.getIsVisible() == PrivacySetting.CLIENT && articleDto.getIsVisible() == PrivacySetting.PUBLIC) {
						updatedArticle.setIsVisible(PrivacySetting.CLIENT);
					}
			}else {			
				updatedArticle.setIsVisible(article1.getIsVisible());
			}
			article1 = articleCompanyRepository.save(updatedArticle); // a verifier 
			return ResponseEntity.ok().body(articleDto);
			}
	
		
		private double round(double value) {
		    return Math.round(value * 100.0) / 100.0; 
		}
		
		public Article findById(Long articleId) {
			Optional<Article>  article = articleRepository.findById(articleId);
			return article.get();
		}
		
		public ArticleCompany findArticleCompanyById(Long id) {
			ArticleCompany article = articleCompanyRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("this article does not exist"));
			return article;
		}
		
		public ResponseEntity<String> deleteByCompanyArticleId(Long articleId, Company provider) {
			ArticleCompany article = articleCompanyRepository.findByIdAndIsDeletedFalse(articleId).orElseThrow(() -> new RecordNotFoundException("This Article Does Not Exist"));	
			article.setQuantity(0.0);
			article.setIsDeleted(true);
			logger.warn("successfully deleted ");
				return ResponseEntity.ok("successfuly deleted");
		}
		
		
		
		
		
		
		

		
	/////////////////////////////////////// future work ////////////////////////////////////////////////////////
	public List<ArticleCompanyWithoutTroubleDto> getByNameContaining( Long providerId, Long userId,String articlenamecontaining, int page , int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC,"lastModifiedDate");
		Pageable pageable = PageRequest.of(page, pageSize, sort);				
		Page<ArticleCompany> article = articleCompanyRepository.findAllByLibelleAndProviderIdContaining(articlenamecontaining,providerId, userId,pageable);
		List<ArticleCompanyWithoutTroubleDto> articleDto = new ArrayList<>();
			if(!article.isEmpty()) {
			Boolean isFav = false;
			for(ArticleCompany i : article.getContent()) {
		if(providerId == null) {
			isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),userId);
		}
		else {
			isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),providerId);
		}
		ArticleCompanyWithoutTroubleDto artDto =  articleCompanyMapper.mapToArticleCompanyWithoutTroubleDto(i);
			artDto.setIsFav(isFav);
			articleDto.add(artDto);
	}
	logger.warn(articleDto.size()+" size article contain");
	}
		return articleDto;

	}

//	public ArticleDto getMyArticleById(Long id, Company company) {
//		Article article = getById(id).getBody();
//		ArticleDto articleDto = articleMapper.mapToDto(article);
//		return articleDto;
//	} because change of article to article company class

	public void addChilToParentArticle(Company company, Long parentId, Long childId, double quantity) {
		ArticleCompany parentArticle = findByArticleCompanyId(parentId);
		if(parentId == childId) {
			throw new NotPermissonException("you can not make article"+parentArticle.getArticle().getLibelle()+" child of "+ parentArticle.getArticle().getLibelle());
		}
		ArticleCompany childArticle = findByArticleCompanyId(childId);
		if((parentArticle.getSubArticle() != null && parentArticle.getSubArticle().stream().anyMatch(branche -> branche.getChildArticle().getId().equals(childId)))||
				( childArticle.getSubArticle() != null &&
				childArticle.getSubArticle().stream().anyMatch(branche -> branche.getChildArticle().getId().equals(parentId)))){
			throw new NotPermissonException("you can not do that");
		}
		if(!parentArticle.getCompany().equals(company) || !childArticle.getCompany().equals(company)) {
			throw new NotPermissonException("you don't have the permission to do that");
		}
		
		SubArticle subArticle = new SubArticle();
		subArticle.setChildArticle(childArticle);
		subArticle.setParentArticle(parentArticle);
		subArticle.setQuantity(quantity);
		subArticleRepository.save(subArticle);
		
		double minQuantity = round(calculateMinQuantity(parentArticle,(long)0,subArticle));
		if(parentArticle.getUnit() == Unit.U) {
			int integerPart = (int) minQuantity;
			logger.warn("integer value " +integerPart);
			 minQuantity = Double.valueOf(integerPart);
			 logger.warn("double value " +minQuantity);
		}
		parentArticle.setQuantity(minQuantity);
		
	}
	
	private double calculateMinQuantity(ArticleCompany parentArticle,Long id, SubArticle childArticle) {
	    double minQuantity = parentArticle.getQuantity();
	    if(!parentArticle.getSubArticle().isEmpty()) {
	    	logger.warn("sub article not null => size "+parentArticle.getSubArticle().size());
	    	Iterator<SubArticle> iterator = parentArticle.getSubArticle().iterator();
	    	SubArticle subArticleWithDifferentId = null; // Initialize the variable to store the matching SubArticle

	    	while (iterator.hasNext()) {
	    	    SubArticle subArticle = iterator.next();
	    	    if (subArticle.getId() != id) {
	    	        subArticleWithDifferentId = subArticle; // Assign the matching SubArticle
	    	        break; // Exit the loop once a matching SubArticle is found
	    	    }
	    	}
	    	if(subArticleWithDifferentId != null) {
	    		
	        minQuantity = round(subArticleWithDifferentId.getChildArticle().getQuantity() / subArticleWithDifferentId.getQuantity());
	    	}
	    	
	    for (SubArticle subArticle : parentArticle.getSubArticle()) {
	    	if(subArticle.getId() != id) {	    		
			logger.warn("sub article id from calcul min function "+subArticle.getId());
	        double quantityRatio = subArticle.getChildArticle().getQuantity() / subArticle.getQuantity();
	        minQuantity = Math.min(minQuantity, quantityRatio);
	    	}
	    }
	    }else {
	    	minQuantity  = childArticle.getChildArticle().getQuantity() / childArticle.getQuantity();
	    	logger.warn(minQuantity+ " min quantity");
	    }
	    
	    return minQuantity;
	}



	public void deleteSubArticle(Long id, Long companyId) {
		SubArticle subArticle = subArticleRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no relation between the two articles"));
		if(subArticle.getParentArticle().getCompany().getId() != companyId) {
			throw new NotPermissonException("you do not have a permission to do that");
		}
	
		subArticleRepository.deleteById(id);
		logger.warn("sub article id from delete function "+subArticle.getId());
		double minQuantity = round(calculateMinQuantity(subArticle.getParentArticle(),id,subArticle));
		subArticle.getParentArticle().setQuantity(minQuantity);
	}

	public ArticleCompany findByArticleCompanyId(Long articleId) {
		Optional<ArticleCompany> article = articleCompanyRepository.findById(articleId);
		if(article.isEmpty()) {
			throw new RecordNotFoundException("this article does not exist");
		}
		return article.get();
	}

	public Boolean existsOne() {
		Boolean exists = articleRepository.existsById(1L);
		return exists;
	}

	public void addDairyArticles() {
		List<Article> articles = new ArrayList<>();
		Article spiga1 = new Article();
		spiga1.setCode("sn1");
		spiga1.setLibelle("spiga N1");
		spiga1.setBarcode("6192011803696");
		spiga1.setImage("spiga1.jpg");
		spiga1.setTva(0.0);
		spiga1.setDiscription("the best of");
		spiga1.setIsDiscounted(true);
		spiga1.setCategory(CompanyCategory.DAIRY);
		articles.add(spiga1);
		
		Article spiga2 = new Article();
		spiga2.setLibelle("spiga N2");
		spiga2.setBarcode("6192011803672");
		spiga2.setCode("sn2");
		spiga2.setImage("spiga2-g.jpg");
		spiga2.setTva(0.0);
		spiga2.setDiscription("the best of");
		spiga2.setIsDiscounted(true);
		spiga2.setCategory(CompanyCategory.DAIRY);
		articles.add(spiga2);

		Article spiga3 = new Article();
		spiga3.setLibelle("spiga N3");
		spiga3.setBarcode("barcode3");
		spiga3.setCode("sn3");
		spiga3.setImage("spiga3.jpg");
		spiga3.setTva(0.0);
		spiga3.setDiscription("the best of");
		spiga3.setIsDiscounted(true);
		spiga3.setCategory(CompanyCategory.DAIRY);
		articles.add(spiga3);

		Article spegetti1 = new Article();
		spegetti1.setLibelle("spageti N1");
		spegetti1.setBarcode("barcode spageti1");
		spegetti1.setCode("spn1");
		spegetti1.setImage("spaghetti1-flottant.png");
		spegetti1.setTva(0.0);
		spegetti1.setDiscription("the best of");
		spegetti1.setIsDiscounted(true);
		spegetti1.setCategory(CompanyCategory.DAIRY);
		articles.add(spegetti1);

		Article spegetti2 = new Article();
		spegetti2.setLibelle("spageti N2");
		spegetti2.setBarcode("barcode spageti2");
		spegetti2.setCode("spn2");
		spegetti2.setImage("spagetti2.png");
		spegetti2.setTva(0.0);
		spegetti2.setDiscription("the best of");
		spegetti2.setIsDiscounted(true);
		spegetti2.setCategory(CompanyCategory.DAIRY);
		articles.add(spegetti2);

		Article spegetti3 = new Article();
		spegetti3.setLibelle("spageti N3");
		spegetti3.setBarcode("barcode spageti3");
		spegetti3.setCode("spn3");
		spegetti3.setImage("spaghetti3-flottant.png");
		spegetti3.setTva(0.0);
		spegetti3.setDiscription("the best of");
		spegetti3.setIsDiscounted(true);
		spegetti3.setCategory(CompanyCategory.DAIRY);
		articles.add(spegetti3);

		Article sicam1 = new Article();
		sicam1.setLibelle("sicam 1kg");
		sicam1.setBarcode("barcode sicam1kg");
		sicam1.setCode("s1kg");
		sicam1.setImage("sicam-800g.jpg");
		sicam1.setTva(0.0);
		sicam1.setDiscription("the best of");
		sicam1.setIsDiscounted(true);
		sicam1.setCategory(CompanyCategory.DAIRY);
		articles.add(sicam1);

		Article sicam2 = new Article();
		sicam2.setLibelle("sicam 0.5kg");
		sicam2.setBarcode("barcode sicam0.5kg");
		sicam2.setCode("s0.5kg");
		sicam2.setImage("sicam-800g.jpg");
		sicam2.setTva(0.0);
		sicam2.setDiscription("the best of");
		sicam2.setIsDiscounted(true);
		sicam2.setCategory(CompanyCategory.DAIRY);
		articles.add(sicam2);
	
		
			
		articleRepository.saveAll(articles);
	}

	public List<ArticleDto> getArticlesByCategory(Long id, CompanyCategory category, int offset , int pageSize) {
			
		Pageable pageable = PageRequest.of(offset, pageSize);
		Page<Article> articles = articleRepository.finAllByCategoryAndCompanyId(category,id,  pageable );
		if(articles.isEmpty()) {
			throw new RecordNotFoundException("you have added all articles");
		}
		List<ArticleDto> articlesDto = new ArrayList<>();
		for(Article i : articles) {
			ArticleDto articleDto = articleMapper.mapToDto(i);
			articlesDto.add(articleDto);
		}
		logger.warn(articlesDto.size()+" size articlesDto");
		return articlesDto;
	}

	public List<ArticleCompanyDto> findRandomArticlesByCompanyCategory(CompanyCategory categname, Company company,User user) {
		List<ArticleCompany> articlesCompany = new ArrayList<>();
		if(company != null) {
		articlesCompany = articleCompanyRepository.findAllByCompanyCategoryAndCompanyId(categname,company.getId(), company.getLatitude(), company.getLongitude());
		}
		if(user != null) {
		articlesCompany = articleCompanyRepository.findAllByCompanyCategoryAndUserId(categname,user.getId(), user.getLatitude(), user.getLongitude());
		}
		if(articlesCompany.isEmpty()) {
			throw new RecordNotFoundException("there is no article with this category"+categname);
		}
		List<ArticleCompanyDto> articlesCompanyDto = new ArrayList<>();
		Boolean isFav = false;
		Boolean isEnabledToComment = false;
		for(ArticleCompany i : articlesCompany) {
			if(company == null) {
				isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),user.getId());
			//	isEnabledToComment = isEnabledToComment(user.getId(), null, i.getCompany().getId());
			}
			else {
				isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),company.getId());
			//	isEnabledToComment = isEnabledToComment(null,company.getId(), i.getCompany().getId());
			}
			ArticleCompanyDto articleCompanyDto = articleCompanyMapper.mapToDto(i);
			articleCompanyDto.setIsFav(isFav);
			articleCompanyDto.setIsEnabledToComment(isEnabledToComment);
			articlesCompanyDto.add(articleCompanyDto);
		}
		return articlesCompanyDto;
	}

	private Boolean isEnabledToCommen(Long myUserId, Long myCompanyId, Long providerId) {
		Boolean exists = false;
		if(myCompanyId == null) {
		//	exists = invoiceRepository.existsByPersonIdAndProviderIdAndIsEnabledToComment(myUserId, providerId, true);
		}else {
		//	exists = invoiceRepository.existsByClientIdAndProviderIdAndIsEnabledToComment(myCompanyId, providerId, true);	
		}
		return exists;
	}
	
	public void addButcherArticles() {
		List<Article> articles = new ArrayList<>();
		Article beuf = new Article();
		beuf.setCode("Beuf");
		beuf.setLibelle("viande beuf");
		beuf.setImage("habre-boeuf.jpg");
		beuf.setTva(0.0);
		beuf.setDiscription("the best of beuf");
		beuf.setIsDiscounted(true);
		beuf.setCategory(CompanyCategory.BUTCHER);
		articles.add(beuf);
		
		Article anne = new Article();
		anne.setCode("Anne");
		anne.setLibelle("viande Anne :)");
		anne.setImage("kotlet-boeuf.jpg");
		anne.setTva(0.0);
		anne.setDiscription("the best of anne");
		anne.setIsDiscounted(true);
		anne.setCategory(CompanyCategory.BUTCHER);
		articles.add(anne);
		

		articleRepository.saveAll(articles);
	}
	
	public void addVegitableArticles() {
		List<Article> articles = new ArrayList<>();
		Article tomate = new Article();
		tomate.setCode("Toamte");
		tomate.setLibelle("Tomate");
		tomate.setImage("tomate.jpg");
		tomate.setTva(0.0);
		tomate.setDiscription("the best of tomate");
		tomate.setIsDiscounted(true);
		tomate.setCategory(CompanyCategory.VEGETABLE);
		articles.add(tomate);
		
		Article felfel = new Article();
		felfel.setCode("Felfel");
		felfel.setLibelle("Felfel");
		felfel.setImage("felfel-baklouti.jpg");
		felfel.setTva(0.0);
		felfel.setDiscription("the best of felfel");
		felfel.setIsDiscounted(true);
		felfel.setCategory(CompanyCategory.VEGETABLE);
		articles.add(felfel);
		

		articleRepository.saveAll(articles);
	}

	public void impactFromOrder(PurchaseOrderLine purchaseOrderLine) {
		ArticleCompany article = super.getById(purchaseOrderLine.getArticle().getId()).getBody();
		article.setQuantity(article.getQuantity()-purchaseOrderLine.getQuantity());
		articleCompanyRepository.save(article);
	}

	public ArticleDto getArticleById(Long id, Company company) {
		Article article = articleRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no article with id :"+id));
		ArticleDto articleDto = articleMapper.mapToDto(article);
		return articleDto;
	}

	public ArticleCompanyDto getArticleByBarcode(Long id, String barcode) {
		ArticleCompany article = articleCompanyRepository.findByBarcodeAndCompanyId(barcode, id);
		if(article == null) {
			throw new RecordNotFoundException("there is no 	article with barcode : "+barcode+" company id "+id);
		}
		ArticleCompanyDto articleCompanyDto = articleCompanyMapper.mapToDto(article);
		return articleCompanyDto;
	}

	public List<ArticleCompanyWithoutTroubleDto> getMyArticleContaining(Long id, String search, int page, int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC,"lastModifiedDate");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Page<ArticleCompany> article = articleCompanyRepository.findAllByCompanyIdAndLibelleContaining(id , search , pageable);
		List<ArticleCompanyWithoutTroubleDto> response = mapListToArticleCompanyWithoutTroubleDto(article.getContent());
		return response;
	}


	private List<ArticleCompanyWithoutTroubleDto> mapListToArticleCompanyWithoutTroubleDto(List<ArticleCompany> articles){
		List<ArticleCompanyWithoutTroubleDto> articlesDto = new ArrayList<>();
		for(ArticleCompany i : articles) {
			ArticleCompanyWithoutTroubleDto articleDto = articleCompanyMapper.mapToArticleCompanyWithoutTroubleDto(i);
			articlesDto.add(articleDto);
		}
		return articlesDto;
	}

	private List<ArticleCompanyDto> mapArticleCompanyListToDto(List<ArticleCompany> articles){
		List<ArticleCompanyDto> articlesDto = new ArrayList<>();
		for(ArticleCompany i : articles) {
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articlesDto.add(articleDto);
		}
		return articlesDto;
	}

	public List<ArticleCompanyDto> getAllCompanyArticlesByCompanyId(Long companyId, int page, int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<ArticleCompany> articleCompany = articleCompanyRepository.findByCompanyId(companyId , pageable);
		List<ArticleCompanyDto> articlesCompanyDto = mapArticleCompanyListToDto(articleCompany.getContent());
		return articlesCompanyDto;
	}

	public List<ArticleCompanyDto> companyArticlesByCategoryOrSubCategory(Long companyId, Long categoryId,Long subCategoryId, Long clientId, Long userId, int page, int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		List<ArticleCompany> articlesCompany = new ArrayList<>();
		if(subCategoryId != 0) {
			Page<ArticleCompany> articleCompany = articleCompanyRepository.findByCompanyIdAndSubCategoryId(companyId , subCategoryId ,clientId, userId, pageable);
			articlesCompany.addAll(articleCompany.getContent());
			
		}else {
		Page<ArticleCompany> articleCompany = articleCompanyRepository.findByCompanyIdAndCategoryId(companyId , categoryId, clientId, userId , pageable);
		articlesCompany.addAll(articleCompany.getContent());
		}
		List<ArticleCompanyDto> response = mapArticleCompanyListToDto(articlesCompany);
		return response;
	}


	


	



	
}
