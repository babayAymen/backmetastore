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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyDto;
import com.example.meta.store.werehouse.Dtos.ArticleDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Category;
import com.example.meta.store.werehouse.Entities.CommandLine;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Inventory;
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
public class ArticleService extends BaseService<Article, Long>{

	private final ArticleRepository articleRepository;
	
	private final ArticleCompanyRepository articleCompanyRepository;

	private final SubArticleRepository subArticleRepository;
	
	private final ArticleMapper articleMapper; 
	
	private final ArticleCompanyMapper articleCompanyMapper;
	
	private final ObjectMapper objectMapper;

	private final InventoryService inventoryService;

	private final ImageService imageService;
	
	private final CategoryService categoryService;
	
	private final SubCategoryService subCategoryService;
	
	
	private final LikeRepository likeRepository;
	private final Logger logger = LoggerFactory.getLogger(ArticleService.class);
	

	/////////////////////////////////////// real work ////////////////////////////////////////////////////////
	public List<ArticleCompanyDto> findRandomArticlesPub( Company myCompany, User user) {
		List<ArticleCompany> article = new ArrayList<>();
		Boolean isFav = false;
		if(myCompany == null) {
		 article = articleCompanyRepository.findRandomArticles(user.getId(),user.getLongitude(), user.getLatitude());
		}
		else {
			article = articleCompanyRepository.findRandomArticlesPro(myCompany.getId(), myCompany.getLongitude(), myCompany.getLatitude());	 
		}
		if(article.isEmpty()) {
			throw new RecordNotFoundException("No Article");
		}
			List<ArticleCompanyDto> articlesDto = new ArrayList<>();
			for(ArticleCompany i:article) {
				if(myCompany == null) {
					isFav = articleCompanyRepository.existsByIdAndUsersId(i.getId(),user.getId());
				}
				else {
					isFav = articleCompanyRepository.existsByIdAndCompaniesId(i.getId(),myCompany.getId());
					logger.warn("is fav is : "+isFav+" for article id : "+i.getId());
				}
			ArticleCompanyDto dto = articleCompanyMapper.mapToDto(i);
			
				dto.setIsFav(isFav);
			articlesDto.add(dto);
	}
			return articlesDto;
	}
	
	public List<ArticleCompanyDto> getAllArticleByCompanyId(Long providerId,Long myClientId,Long myCompanyId, int offset, int pageSize) {
		
		Pageable pageable = PageRequest.of(offset, pageSize);
		Page<ArticleCompany> articles;
		if(providerId == myCompanyId) {
			articles = articleCompanyRepository.findAllByCompanyIdOrderByCreatedDateDesc(myCompanyId,pageable);			
		}else {			
		 articles = articleCompanyRepository.findAllByCompanyId(myCompanyId,myClientId,providerId,pageable);
		}
		if(articles.isEmpty()) {
			throw new RecordNotFoundException("there is no article");
		}
		List<ArticleCompanyDto> articlesDto = new ArrayList<>();
		for(ArticleCompany i : articles) {
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			logger.warn(" child article id 2");
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
	
	public List<ArticleCompanyDto> getAllProvidersArticleByProviderId(Company company, Long id, int offset, int pageSize) {
		logger.warn("articles size 1 ");
		List<ArticleCompanyDto> articlesDto = new ArrayList<ArticleCompanyDto>();
		logger.warn("articles size 2 ");
		Page<ArticleCompany> articles = null;
		logger.warn("articles size 3 ");
		Pageable pageable = PageRequest.of(0,20);// a return
		if(company.getId() != id) {
			for(Company i : company.getBranches()) {
				if(i.getId() == id) {
					articles = articleCompanyRepository.findAllByCompanyIdOrderByCreatedDateDesc(id,pageable);		
				}
			}
		}else {
			logger.warn("articles size ");
		 articles = articleCompanyRepository.findAllByCompanyIdOrderByCreatedDateDesc(company.getId(),pageable);
		 logger.warn("articles size "+articles.getSize());
		 logger.warn("index of : "+articles.stream().toString().indexOf(1));
		}
		if(articles != null) {
			List<ArticleCompany> articlesContent = articles.getContent();
			for(ArticleCompany i : articlesContent) {
				ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articlesDto.add(articleDto);
			
			}
		}
		logger.warn("size of rerturn : "+articlesDto.size());
		return articlesDto;
	}
	
	public List<ArticleCompanyDto> getAllArticleByCategoryId(Long categoryId, Long companyId,User user, Company client) {
		List<ArticleCompany> articles;
		if(client != null) {
		if( companyId == client.getId()) {
			logger.warn("getAllArticleByCategoryId mrigel inside for loop ");
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
		for(ArticleCompany i : articles) {
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articlesDto.add(articleDto);
		}
		return articlesDto;
	}
	
	public List<ArticleCompanyDto> getAllArticleBySubCategoryIdAndCompanyId(Long subcategoryId, Long companyId,User user, Company client) {
		List<ArticleCompany> articles;
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
			ArticleCompanyDto articleDto = articleCompanyMapper.mapToDto(i);
			articlesDto.add(articleDto);
		}
		return articlesDto;
	}
	
	
	
	   public ResponseEntity<ArticleDto> insertArticle( MultipartFile file, String article, Company provider, Long articleId)
				throws JsonMappingException, JsonProcessingException {
			ArticleCompany article1 = objectMapper.readValue(article, ArticleCompany.class);
//			if(file != null) {
//				String newFileName = imageService.insertImag(file,provider.getUser().getId(), "article");// the user id must be change because it does not make sense
//				article1.setImage(newFileName);
//			}
			Boolean existRelation = articleCompanyRepository.existsByArticleIdAndCompanyId(articleId, provider.getId());
			if(existRelation) {
				return null;
			}
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
//			article1.setSharedPoint(provider.getUser().getId());
			if(provider.getIsVisible() == PrivacySetting.ONLY_ME) {	
			article1.setIsVisible(PrivacySetting.ONLY_ME);
			}
			if(provider.getIsVisible() == PrivacySetting.CLIENT && article1.getIsVisible() == PrivacySetting.PUBLIC) {
				article1.setIsVisible(PrivacySetting.CLIENT);
			}
			article1.setCompany(provider);	
			inventoryService.makeInventory(article1, provider);
			articleCompanyRepository.save(article1);
			return ResponseEntity.ok(null);
		}
	
		public void addQuantity(Long id, Double quantity, Company provider) {
			ArticleCompany article = articleCompanyRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no article with id: "+id));
			Double Quantity = round(article.getQuantity()+quantity);
			article.setQuantity(Quantity);
			articleCompanyRepository.save(article);
			inventoryService.addQuantity(article,quantity, provider);

		}
	
		public ResponseEntity<ArticleCompanyDto> upDateArticle( MultipartFile file, String article, Company provider) 
				throws JsonMappingException, JsonProcessingException {
			ArticleCompanyDto articleDto = objectMapper.readValue(article, ArticleCompanyDto.class);
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
			updatedArticle.setSharedPoint(provider.getUser().getId());
			article1 = articleCompanyRepository.save(updatedArticle); // a verifier 
			return null;
			}
	
		
		private double round(double value) {
		    return Math.round(value * 100.0) / 100.0; 
		}
		
		public Article findById(Long articleId) {
			Optional<Article>  article = articleRepository.findById(articleId);
			return article.get();
		}
		
		public ResponseEntity<String> deleteByCompanyArticleId(Long articleId, Company provider) {
			Article article = articleRepository.findById(articleId).orElseThrow(() -> new RecordNotFoundException("This Article Does Not Exist"));							
//				if(article.getProvider().getId() == provider.getId() ||
//						article.getProvider().isVirtual() == true &&
//								article.getProvider() == provider ) {
//					Inventory inventory = inventoryService.findByArticleIdAndCompanyId(articleId, provider.getId());
//					if(inventory.getOut_quantity() == 0) {
//					inventory.setArticle(null);	
//					inventoryService.deleteById(inventory.getId());
//					articleRepository.deleteById(articleId);
//					}
//					else {
//						article.setProvider(null);
//						articleRepository.save(article);
//					}
//				}
			//delete autrement
				return ResponseEntity.ok("successfuly deleted");
		}
		
		
		
		
		
		
		

		
	/////////////////////////////////////// future work ////////////////////////////////////////////////////////
	public List<ArticleCompanyDto> getByNameContaining(String articlenamecontaining, Long providerId, Long userId, SearchType type) {
		List<ArticleCompany> article = new ArrayList<>();
				if(type == SearchType.OTHER) {					
		article = articleCompanyRepository.findAllByLibelleAndProviderIdContaining(articlenamecontaining,providerId, userId);
				}else {
					article = articleCompanyRepository.findAllMyByLibeleContaining(articlenamecontaining,providerId);
				}
		
		if(!article.isEmpty()) {
	List<ArticleCompanyDto> articleDto = new ArrayList<>();
	for(ArticleCompany i : article) {
			ArticleCompanyDto artDto =  articleCompanyMapper.mapToDto(i);
			articleDto.add(artDto);
	}
	logger.warn(articleDto.size()+" size article contain");
	return articleDto;
	}
		throw new RecordNotFoundException("there is no record cointaining "+articlenamecontaining +" provide id: "+providerId);

	}

	public ArticleDto getMyArticleById(Long id, Company company) {
		Article article = getById(id).getBody();
		ArticleDto articleDto = articleMapper.mapToDto(article);
		return articleDto;
	}

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
		spiga1.setBarcode("barcode1");
		spiga1.setImage("spiga1.jpg");
		spiga1.setTva(0.0);
		spiga1.setDiscription("the best of");
		spiga1.setIsDiscounted(true);
		spiga1.setCategory(CompanyCategory.DAIRY);
		articles.add(spiga1);
		
		Article spiga2 = new Article();
		spiga2.setLibelle("spiga N2");
		spiga2.setBarcode("barcode2");
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

	public List<ArticleDto> getArticlesByCategory(Long id, CompanyCategory category) {
		logger.warn("category company "+category);
		List<Article> articles = articleRepository.finAllByCategoryAndCompanyId(category, id);
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
		for(ArticleCompany i : articlesCompany) {
			ArticleCompanyDto articleCompanyDto = articleCompanyMapper.mapToDto(i);
			articlesCompanyDto.add(articleCompanyDto);
		}
		return articlesCompanyDto;
	}

	public void addButcherArticles() {
		List<Article> articles = new ArrayList<>();
		Article beuf = new Article();
		beuf.setCode("Beuf");
		beuf.setLibelle("viande beuf");
		beuf.setImage("spiga1.jpg");
		beuf.setTva(0.0);
		beuf.setDiscription("the best of beuf");
		beuf.setIsDiscounted(true);
		beuf.setCategory(CompanyCategory.BUTCHER);
		articles.add(beuf);
		
		Article anne = new Article();
		anne.setCode("Anne");
		anne.setLibelle("viande Anne :)");
		anne.setImage("spiga1.jpg");
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
		tomate.setImage("spiga1.jpg");
		tomate.setTva(0.0);
		tomate.setDiscription("the best of tomate");
		tomate.setIsDiscounted(true);
		tomate.setCategory(CompanyCategory.VEGETABLE);
		articles.add(tomate);
		
		Article felfel = new Article();
		felfel.setCode("Felfel");
		felfel.setLibelle("Felfel");
		felfel.setImage("spiga1.jpg");
		felfel.setTva(0.0);
		felfel.setDiscription("the best of felfel");
		felfel.setIsDiscounted(true);
		felfel.setCategory(CompanyCategory.VEGETABLE);
		articles.add(felfel);
		

		articleRepository.saveAll(articles);
	}





	


	



	
}
