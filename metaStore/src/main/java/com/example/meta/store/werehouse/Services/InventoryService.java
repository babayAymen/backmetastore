package com.example.meta.store.werehouse.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.CommandLineDto;
import com.example.meta.store.werehouse.Dtos.InventoryDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.CommandLine;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Inventory;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;
import com.example.meta.store.werehouse.Mappers.InventoryMapper;
import com.example.meta.store.werehouse.Repositories.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService extends BaseService<Inventory, Long> {

	private final InventoryMapper inventoryMapper;
	
	private final InventoryRepository inventoryRepository;
		

	private final Logger logger = LoggerFactory.getLogger(InventoryService.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	public List<InventoryDto> getInventoryByCompanyId(Long companyId) {
		List<Inventory> inventory = inventoryRepository.findByCompanyId(companyId);
		if(inventory == null) {
			throw new RecordNotFoundException("You Dont Have A Company Please Create One If You Need");
		}
		List<InventoryDto> inventoriesDto = new ArrayList<>();
		for(Inventory i:inventory) {
			InventoryDto inventoryDto = inventoryMapper.mapToDto(i);
			inventoriesDto.add(inventoryDto);
		}
		return inventoriesDto;
	}
	public void addQuantity(ArticleCompany article, Double quantity, Company company) {
		Optional<Inventory> inventori = inventoryRepository.findByArticleIdAndCompanyId(article.getId(),company.getId());
		Inventory inventory = inventori.get();
		Double articleCost = sumWithTwoValue(
				multipleWithTwoValue(multipleWithTwoValue(article.getCost(), sumWithTwoValue(1.0
						, multipleWithTwoValue(article.getArticle().getTva()
								, 0.01))) 
						, quantity) 
				, inventory.getArticleCost());
		inventory.setIn_quantity(inventory.getIn_quantity()+quantity);
		inventory.setArticleCost(articleCost);
		inventoryRepository.save(inventory);
		
	}
	
	
	private Optional<Inventory> findByArticleIdAndCompanyId(Long articleId, Long companyId) {
		Optional<Inventory> inventory = inventoryRepository.findByArticleIdAndCompanyId(articleId, companyId);
		return inventory;
	}
	
	public void impactInvoiceOnClient(Company company, CommandLine i, ArticleCompany article) {
		Optional<Inventory> clientInventory = inventoryRepository.findByArticleIdAndCompanyId(article.getId(),company.getId());
		Double articleCost = multipleWithTwoValue(i.getArticle().getSellingPrice() , i.getQuantity());
		Inventory clientInventori ;
		if(clientInventory.isPresent()) {
			clientInventori = clientInventory.get();
			clientInventori.setIn_quantity(sumWithTwoValue(clientInventori.getIn_quantity(),i.getQuantity()));
			Double cumuleArticleCost = sumWithTwoValue(clientInventori.getArticleCost(),articleCost);
			clientInventori.setArticleCost(cumuleArticleCost);
			if(i.getDiscount() !=null) {
				Double articleDiscount = sumWithTwoValue(clientInventori.getDiscountIn() , multipleWithTwoValue(article.getCost(),multipleWithTwoValue(i.getDiscount(), 0.01)));
				clientInventori.setDiscountIn(articleDiscount);
			}
			if(i.getInvoice().getDiscount() != null) {
				double articleDiscount = sumWithTwoValue(clientInventori.getDiscountIn() , multipleWithTwoValue(article.getCost() , multipleWithTwoValue(i.getInvoice().getDiscount(), 0.01)));
				clientInventori.setDiscountIn(articleDiscount);
			}
		}else {
			clientInventori = new Inventory();
			clientInventori.setArticle(article);
			clientInventori.setCompany(company);
			clientInventori.setArticleCost(articleCost);
			clientInventori.setArticleSelling(0.0);
			clientInventori.setOut_quantity(0.0);
			clientInventori.setDiscountIn(0.0);
			clientInventori.setDiscountOut(0.0);
			clientInventori.setIn_quantity(i.getQuantity());
			if(i.getDiscount() !=null) {				
				Double articleDiscount = sumWithTwoValue(clientInventori.getDiscountIn() , multipleWithTwoValue(i.getArticle().getCost() , multipleWithTwoValue(i.getDiscount(), 0.01)));
				clientInventori.setDiscountIn(articleDiscount);
			}
			if(i.getInvoice().getDiscount() != null) {
				Double articleDiscount = sumWithTwoValue(clientInventori.getDiscountIn() , multipleWithTwoValue(i.getArticle().getCost() , multipleWithTwoValue(i.getInvoice().getDiscount(), 0.01)));
				clientInventori.setDiscountIn(articleDiscount);
			}
		}
		inventoryRepository.save(clientInventori);
	}

	public void rejectInvoice(List<CommandLine> commandLines, Long companyId) {
		for(CommandLine i : commandLines) {
			Optional<Inventory> inventory = inventoryRepository.findByArticleIdAndCompanyId(i.getArticle().getId(),companyId);
			if(inventory.isPresent()) {
				Inventory inventori = inventory.get();
				inventori.setOut_quantity(sumWithTwoValue(inventori.getOut_quantity() , -i.getQuantity()));
				inventoryRepository.save(inventori);
			}
		}
	}
	
	public ResponseEntity<InventoryDto> makeInventory(ArticleCompany article, Company company){
		Inventory inventory = new Inventory();
		Double articleCost = multipleWithTwoValue(sumWithTwoValue(article.getCost() , multipleWithTwoValue(article.getCost() , multipleWithTwoValue(article.getArticle().getTva(), 0.01))) , article.getQuantity());
		inventory.setIn_quantity(article.getQuantity());
		inventory.setCompany(company);
		inventory.setOut_quantity(0.0);
		inventory.setArticle(article);
		inventory.setArticleCost(articleCost);
		inventory.setArticleSelling(0.0);
		inventory.setDiscountIn(0.0);
		inventory.setDiscountOut(0.0);
		inventoryRepository.save(inventory);
		return null;
		
	}
	
	public void impacteInvoice( Company company, List<CommandLine> commandLinesDto) {
		for(CommandLine i : commandLinesDto) {
			Optional<Inventory> providerInventori = findByArticleIdAndCompanyId(i.getArticle().getId(),company.getId());
			if(!providerInventori.isEmpty()) {
				Inventory providerInventory = providerInventori.get();
			providerInventory.setOut_quantity(sumWithTwoValue(providerInventory.getOut_quantity(),i.getQuantity()));
			if(i.getDiscount() != 0) {
				Double articleDiscount = sumWithTwoValue(providerInventory.getDiscountOut(),multipleWithTwoValue(i.getArticle().getSellingPrice(),i.getDiscount()/100));
				providerInventory.setDiscountOut(articleDiscount);
			}
			if(i.getInvoice().getDiscount() !=0) {
				Double invoiceDiscount = sumWithTwoValue(providerInventory.getDiscountOut(),multipleWithTwoValue(i.getArticle().getSellingPrice(),i.getInvoice().getDiscount()/100));
				providerInventory.setDiscountOut(invoiceDiscount);			
			}
			Double articleSelling = sumWithTwoValue(multipleWithTwoValue(i.getArticle().getSellingPrice(),i.getQuantity()),providerInventory.getArticleSelling());
			providerInventory.setArticleSelling(articleSelling);
			inventoryRepository.save(providerInventory);	
			}
		}
	}
	
	private double round(double value) {
		return Math.round(value * 100.0) / 100.0; 
	}
	
	private Double multipleWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.multiply(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	private Double sumWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.add(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	/////////////////////////////////////////////////////// not work ///////////////////////////////////////////////////
	public void updateArticle(Long companyarticleId, Double deference, Company company) {
		Optional<Inventory> inventori = inventoryRepository.findByArticleIdAndCompanyId(companyarticleId,company.getId());
		Inventory inventory = inventori.get();
		if(deference!=0) {
			inventory.setIn_quantity(sumWithTwoValue(inventory.getIn_quantity() , -deference));
		}
		inventoryRepository.save(inventory);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void impactOnSubArticle(ArticleCompany childArticle, Double childQuantity) {
		Optional<Inventory> inventori = findByArticleIdAndCompanyId(childArticle.getId(), childArticle.getCompany().getId());
		if(inventori.isPresent()) {
			Inventory inventory = inventori.get();
		Double inventoryQuantity = sumWithTwoValue(inventory.getOut_quantity() , childQuantity);
		inventory.setOut_quantity(inventoryQuantity);
		Double inventorySelling = sumWithTwoValue(inventory.getArticleSelling() , multipleWithTwoValue(childArticle.getSellingPrice() , childQuantity));
		inventory.setArticleSelling(inventorySelling);
		}
		
	}
	public void impactAcceptingOrderOnInventory(PurchaseOrderLine purchaseOrderLine) {
		Optional<Inventory> inventori = findByArticleIdAndCompanyId(purchaseOrderLine.getArticle().getId(), purchaseOrderLine.getPurchaseorder().getCompany().getId());
		if(inventori.isPresent()) {
			Inventory inventory = inventori.get();
			updateInventory(inventory,purchaseOrderLine.getQuantity());
		}
		else {
			makeNewInventory(purchaseOrderLine.getArticle().getSellingPrice(), purchaseOrderLine.getQuantity());
		}
		if(purchaseOrderLine.getPurchaseorder().getClient() != null) {
			Optional<Inventory> inventorie = findByArticleIdAndCompanyId(purchaseOrderLine.getArticle().getId(), purchaseOrderLine.getPurchaseorder().getCompany().getId());
			if(inventorie.isPresent()) {
				Inventory inventory = inventorie.get();
				updateInventory(inventory,purchaseOrderLine.getQuantity());
			}
			else {
				makeNewInventory(purchaseOrderLine.getArticle().getSellingPrice(), purchaseOrderLine.getQuantity());
			}
		}
	}

	private void makeNewInventory(Double articleSelling, Double quantity) {
		Inventory inventory = new Inventory();
		inventory.setArticleSelling(multipleWithTwoValue(articleSelling , quantity));
		inventory.setDiscountOut(multipleWithTwoValue(articleSelling , multipleWithTwoValue(quantity , 0.1)));
		inventory.setOut_quantity(quantity);
		inventoryRepository.save(inventory);
	}
	
	private void updateInventory(Inventory inventory , Double quantity) {
		inventory.setArticleSelling(sumWithTwoValue(inventory.getArticleSelling() , multipleWithTwoValue(inventory.getArticle().getSellingPrice() , quantity)));
		inventory.setDiscountOut(sumWithTwoValue(inventory.getDiscountOut(), multipleWithTwoValue(inventory.getArticle().getSellingPrice() , multipleWithTwoValue(quantity , 0.1))));
		inventory.setOut_quantity(sumWithTwoValue(inventory.getOut_quantity() , quantity));
	}

	

}
