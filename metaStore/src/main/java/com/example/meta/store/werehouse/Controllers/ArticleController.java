package com.example.meta.store.werehouse.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyDto;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyWithoutTroubleDto;
import com.example.meta.store.werehouse.Dtos.ArticleDto;
import com.example.meta.store.werehouse.Dtos.CommentDto;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Services.ArticleService;
import com.example.meta.store.werehouse.Services.CommentServcie;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.WorkerService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/werehouse/article/")
@RequiredArgsConstructor
public class ArticleController {


	private final ArticleService articleService;
	
	private final UserService userService;
	
	private final CompanyService companyService;
	
	private final CommentServcie commentService;
	
	private final JwtAuthenticationFilter authenticationFilter;
	
	private final WorkerService workerService;

	private final Logger logger = LoggerFactory.getLogger(ArticleController.class);
	
	
	/////////////////////////////////////// real work ////////////////////////////////////////////////////////
	@GetMapping("getrandom")
	public Page<ArticleCompanyDto> findRandomArticles( @RequestParam CompanyCategory category , @RequestParam int offset, @RequestParam int pageSize){
		User user = userService.getUser();
		logger.warn("account type "+authenticationFilter.accountType);
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company myCompany = new Company();
		if(user.getRole() == RoleEnum.ADMIN) {
			 myCompany = companyService.getCompany();
		}
		if(user.getRole() == RoleEnum.WORKER) {
			myCompany = workerService.findCompanyByWorkerId(user.getId()).get();
		}
		return articleService.findRandomArticlesPub(myCompany, user,offset, pageSize, category);
		}
		return articleService.findRandomArticlesPub(null, user,offset, pageSize, category);
	}
	
	@GetMapping("getrandom/{categname}")
	public List<ArticleCompanyDto> findRandomArticlesByCompanyCategory(@PathVariable CompanyCategory categname){
		List<ArticleCompanyDto> articlesCompanyDto = new ArrayList<>();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			articlesCompanyDto = articleService.findRandomArticlesByCompanyCategory(categname, company, null);
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			articlesCompanyDto = articleService.findRandomArticlesByCompanyCategory(categname, null, user); 
		}
		return articlesCompanyDto;
	}
	
	@GetMapping("get_all_articles/{id}")
	public List<ArticleCompanyDto> getAllArticleByProviderId(@PathVariable Long id, @RequestParam int offset, @RequestParam int pageSize){
			if(authenticationFilter.accountType == AccountType.COMPANY) {				 
				Company client = companyService.getCompany();
				return articleService.getAllArticleByCompanyId(id,null,client.getId() ,offset, pageSize);
			}else {
				User user = userService.getUser();
				return articleService.getAllArticleByCompanyId(id,user.getId(),null ,offset, pageSize);
			}
	}
	
	@GetMapping("get_all_my_article/{id}")
	public Page<ArticleCompanyDto> getAllMyArticle(@PathVariable Long id, @RequestParam int offset, @RequestParam int pageSize) {
		User user = userService.getUser();
		Company company = new Company();
		if(user.getRole() == RoleEnum.WORKER) {
			company = workerService.findCompanyByWorkerId(user.getId()).get();
		}else {			
			company = companyService.getCompany();
		}
		if(company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) {
		return articleService.getAllProvidersArticleByProviderId(id,offset, pageSize);
		}
		return null;
	}
	
	
	@GetMapping("category/{categoryId}/{companyId}")
	public List<ArticleCompanyDto> getAllArticelsByCategoryId(@PathVariable Long categoryId, @PathVariable Long companyId){
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			return articleService.getAllArticleByCategoryId(categoryId, companyId,null, company);			
		}
		User user = userService.getUser();
		return articleService.getAllArticleByCategoryId(categoryId, companyId, user,null);					
	}
	
	@GetMapping("subcategory/{subcategoryId}/{companyId}")
	public List<ArticleCompanyDto> getAllArticleBySubCategoryIdAnd( @PathVariable Long subcategoryId, @PathVariable Long companyId) {
		if(authenticationFilter.accountType == AccountType.COMPANY) {		
			Company company = companyService.getCompany();
			return articleService.getAllArticleBySubCategoryIdAndCompanyId(subcategoryId, companyId,null,company);
		}				
		User user = userService.getUser();
		return articleService.getAllArticleBySubCategoryIdAndCompanyId(subcategoryId, companyId,user, null);
	
	}
	
	
	@PostMapping("add/{id}")
	public ResponseEntity<ArticleCompanyDto> insertArticle( @PathVariable Long id , @RequestBody ArticleCompanyDto article){
		Company provider = companyService.getCompany();
		return articleService.insertArticle(article,provider,id);
	}
	
	@GetMapping("{id}/{quantity}")
	public void addQuantity(@PathVariable Double quantity, @PathVariable Long id) {
		Company provider = companyService.getCompany();
		articleService.addQuantity(id,quantity,provider);
	}
	
	
	@PutMapping("update")
	public ResponseEntity<ArticleCompanyDto> upDateArticle(
			 @RequestBody ArticleCompanyDto article){
		Company provider = companyService.getCompany();
		logger.warn("article: "+article.toString());
		return articleService.upDateArticle(article, provider);
	}
	

	@DeleteMapping("delete/{id}")
	public ResponseEntity<String> deleteArticleById(@PathVariable Long id){
		Company provider = companyService.getCompany();
		return articleService.deleteByCompanyArticleId(id,provider);
	}
	
	@GetMapping("my_article/{id}")
	public ArticleDto getMyArticleById(@PathVariable Long id) {
		Company company = companyService.getCompany();
		return articleService.getArticleById(id, company);
		
	}
	
	@GetMapping("child/{parentId}/{childId}/{quantity}")
	public void addChildToParentArticle(@PathVariable Long parentId, @PathVariable Long childId, @PathVariable double quantity) {
		Company company = companyService.getCompany();
		articleService.addChilToParentArticle(company, parentId, childId, quantity);		
	}
	
	@GetMapping("delete_sub/{id}")
	public void deleteSubArticle(@PathVariable Long id) {
		Long companyId = companyService.getCompany().getId();
		 articleService.deleteSubArticle(id,companyId);
	}
	
	@PostMapping("sendComment")
	public void sendComment(@RequestBody CommentDto comment) {
		logger.warn("comment is : "+comment);
		ArticleCompany article = articleService.findByArticleCompanyId(comment.getArticle().getId());
		article.setCommentNumber(article.getCommentNumber()+1);
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			commentService.addComment(article, comment.getContent(), null, company);
			return;
		}
		User user = userService.getUser();
		commentService.addComment(article, comment.getContent(), user, null);
	}
	
	@GetMapping("get_comments/{articleId}")
	public Page<CommentDto> getAllComments(@PathVariable Long articleId, @RequestParam int page , @RequestParam int pageSize){
		return commentService.getAllCommentsByArticleId(articleId, page, pageSize);
	}
	
	@GetMapping("search/{id}")
	public List<ArticleCompanyWithoutTroubleDto> getByNameContaining(@PathVariable Long id ,@RequestParam String search , @RequestParam SearchType searchType, @RequestParam int page , @RequestParam int pageSize){
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company  = new Company();
			if(user.getRole() == RoleEnum.WORKER) {
				company = workerService.findCompanyByWorkerId(user.getId()).get();
			}else {
				company = companyService.getCompany();
			}
			if((company.getId() == id || company.getBranches().stream().anyMatch(branche -> branche.getId().equals(id))) && searchType == SearchType.MY) {
				return articleService.getMyArticleContaining(id, search, page , pageSize);
			}
			
		}
		return articleService.getByNameContaining(null , user.getId(), search , page, pageSize );
	}
	
	@GetMapping("get_articles_by_category/{id}")
	public List<ArticleDto> getArticlesByCategory(@PathVariable Long id, @RequestParam int page, @RequestParam int pageSize  ){
		Company company = companyService.getCompany();
		return articleService.getArticlesByCategory(company.getId(),company.getCategory(), page, pageSize);
	}
	
	@GetMapping("get_by_barcode")
	public ArticleCompanyDto getArticleByBarcode(@RequestParam String barcode){
		User user = userService.getUser();
		Company company = new Company();
		if(user.getRole() == RoleEnum.WORKER) {
			company = workerService.findCompanyByWorkerId(user.getId()).get();
		}else {
			company = companyService.getCompany();
		}
		return articleService.getArticleByBarcode(company.getId(), barcode);
	}
	
	@GetMapping("get_company_article_by_company_id/{companyId}")
	public List<ArticleCompanyDto> getAllCompanyArticles(@PathVariable Long companyId , @RequestParam int page , @RequestParam int pageSize){
		return articleService.getAllCompanyArticlesByCompanyId(companyId , page, pageSize);
	}
	
	@GetMapping("get_company_article_by_category_or_subcategory/{companyId}")
	public List<ArticleCompanyDto> companyArticlesByCategoryOrSubCategory(@PathVariable Long companyId, @RequestParam Long categoryId , @RequestParam Long subCategoryId, @RequestParam int page, @RequestParam int pageSize){
		Long clientId = null;
		Long userId = null;
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			clientId = company.getId();
		}
		if(authenticationFilter.accountType == AccountType.USER) {
			User user = userService.getUser();
			userId = user.getId();
		}
		return articleService.companyArticlesByCategoryOrSubCategory(companyId ,categoryId, subCategoryId,clientId , userId, page , pageSize);
	}
   
}







