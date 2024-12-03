package com.example.meta.store.werehouse.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.ArticleCompanyDto;
import com.example.meta.store.werehouse.Dtos.ArticleDto;
import com.example.meta.store.werehouse.Dtos.CommentDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.CompanyCategory;
import com.example.meta.store.werehouse.Enums.SearchType;
import com.example.meta.store.werehouse.Services.ArticleService;
import com.example.meta.store.werehouse.Services.CommentServcie;
import com.example.meta.store.werehouse.Services.CompanyService;

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

	private final Logger logger = LoggerFactory.getLogger(ArticleController.class);
	
	
	/////////////////////////////////////// real work ////////////////////////////////////////////////////////
	@GetMapping("getrandom")
	public List<ArticleCompanyDto> findRandomArticles( @RequestParam int offset, @RequestParam int pageSize){
		User user = userService.getUser();
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company myCompany = companyService.getCompany();
			return articleService.findRandomArticlesPub(myCompany, user,offset, pageSize);
		}
		return articleService.findRandomArticlesPub(null, user,offset, pageSize);
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
	
	@GetMapping("getAllMyArticle/{id}/{offset}/{pageSize}")
	public List<ArticleCompanyDto> getAllMyArticle(@PathVariable Long id, @PathVariable int offset, @PathVariable int pageSize) {
		Company company = companyService.getCompany();
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
	public ResponseEntity<ArticleDto> insertArticle(
			 @RequestParam(value ="file", required = false) MultipartFile file,
			 @RequestParam("article") String article,
			 @PathVariable Long id)
			throws Exception{
		Company provider = companyService.getCompany();
		return articleService.insertArticle(file,article,provider,id);
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
	
	@PostMapping("sendComment/{articleId}")
	public void sendComment(@RequestBody String comment, @PathVariable Long articleId) {
		ArticleCompany article = articleService.findByArticleCompanyId(articleId);
		article.setCommentNumber(article.getCommentNumber()+1);
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			Company company = companyService.getCompany();
			commentService.addComment(article, comment, null, company);
			return;
		}
		User user = userService.getUser();
		commentService.addComment(article, comment, user, null);
	}
	
	@GetMapping("get_comments/{articleId}")
	public List<CommentDto> getAllComments(@PathVariable Long articleId){
		return commentService.getAllCommentsByArticleId(articleId);
	}
	
	@GetMapping("search/{articlenamecontaining}/{type}")
	public List<ArticleCompanyDto> getByNameContaining(@PathVariable String articlenamecontaining , @PathVariable SearchType type){
		User user = userService.getUser();
		Long companyId = null;
		if(authenticationFilter.accountType == AccountType.COMPANY) {
			companyId  = companyService.getCompany().getId();	
		}
		return articleService.getByNameContaining(articlenamecontaining,companyId, user.getId(), type);
	}
	
	@GetMapping("get_articles_by_category/{id}")
	public List<ArticleDto> getArticlesByCategory(@PathVariable Long id, @RequestParam int page, @RequestParam int pageSize  ){
		Company company = companyService.getCompany();
		return articleService.getArticlesByCategory(company.getId(),company.getCategory(), page, pageSize);
	}
	
	@GetMapping("get_by_barcode")
	public ArticleCompanyDto getArticleByBarcode(@RequestParam String barcode){
		Company company = companyService.getCompany();
		return articleService.getArticleByBarcode(company.getId(), barcode);
	}

}







