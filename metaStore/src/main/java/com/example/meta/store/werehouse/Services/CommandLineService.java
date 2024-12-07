package com.example.meta.store.werehouse.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.CommandLineDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.ClientProviderRelation;
import com.example.meta.store.werehouse.Entities.CommandLine;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Entities.SubArticle;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.InvoiceMode;
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Mappers.CommandLineMapper;
import com.example.meta.store.werehouse.Repositories.CommandLineRepository;
import com.example.meta.store.werehouse.Repositories.ClientProviderRelationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommandLineService extends BaseService<CommandLine, Long> {

	
	private final CommandLineMapper commandLineMapper;

	private final InvoiceService invoiceService;
	
	private final ArticleService articleService;
	
	private final InventoryService inventoryService;
	
	private final CommandLineRepository commandLineRepository;
	
	private final ClientProviderRelationRepository  clientCompanyRRepository;
	
	private final Logger logger = LoggerFactory.getLogger(CommandLineService.class);


	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	public ResponseEntity<InputStreamResource> insertLine(List<CommandLineDto> commandLinesDto, Company company, 
			Long clientId, Double discount, String type, AccountType clientType, InvoiceMode invoiceMode) {
		List<CommandLine> commandLines = new ArrayList<>();
		Invoice invoice = new Invoice() ;
		if(invoiceMode == InvoiceMode.CREATE) {
			invoice = invoiceService.addInvoice(company,clientId,clientType, commandLinesDto.get(0).getInvoice());
		}
		if(invoiceMode == InvoiceMode.UPDATE){
			invoice = invoiceService.getById(commandLinesDto.get(0).getInvoice().getId()).getBody();
			if((invoice.getStatus() != Status.ACCEPTED || (invoice.getClient() != null && invoice.getClient().isVirtual())) && invoice.getProvider().getId() == company.getId()) {
			commandLineRepository.deleteAllByInvoiceId(invoice.getId());
			}else {return null;	}	}
		invoice.setDiscount(discount);
		for(CommandLineDto i : commandLinesDto) {
			ArticleCompany article = articleService.findByArticleCompanyId(i.getArticle().getId());
			if(article.getQuantity()-i.getQuantity()<0) {
				throw new RecordNotFoundException("There Is No More "+article.getArticle().getLibelle());
			}
			article.setQuantity(article.getQuantity() - i.getQuantity());
			CommandLine commandLine = commandLineMapper.mapToEntity(i);
			commandLine.setInvoice(invoice);
			double prix_article_tot = multipleWithTwoValue(i.getQuantity() , article.getSellingPrice());
			commandLine.setPrixArticleTot(prix_article_tot);
			double tot_tva = multipleWithTwoValue(article.getArticle().getTva(),prix_article_tot/100);
			commandLine.setTotTva(tot_tva);
			commandLines.add(commandLine);
			if(article.getSubArticle() != null) {
				impactOnSubArticle(article,i.getQuantity());
			}		}
		super.insertAll(commandLines);
		List<CommandLine> commandLine = commandLineRepository.findAllByInvoiceId(invoice.getId());
		double totHt= 0;
		double totTva= 0;
		double totTtc= 0;
		for(CommandLine i : commandLine) {
			totHt = sumWithTwoValue(totHt, i.getPrixArticleTot());
			totTva = sumWithTwoValue(totTva, i.getTotTva());
			totTtc = sumWithTwoValue(totHt, totTva);
		}
		ClientProviderRelation clientCompany = new ClientProviderRelation(); 
		if(clientType == AccountType.COMPANY) {
			clientCompany = clientCompanyRRepository.findByClientIdAndProviderId(clientId, company.getId()).get();
			if(clientCompany.getClient().isVirtual()) {
				invoice.setStatus(Status.ACCEPTED);
			}		}
		if(clientType == AccountType.USER) {
			logger.warn("person id "+clientId + " provider id "+company.getId());
			clientCompany = clientCompanyRRepository.findByPersonIdAndProviderId(clientId, company.getId()).get();
		}
		Double defference = minesTwoValue(totTtc, invoice.getPrix_invoice_tot()!= null ? invoice.getPrix_invoice_tot() : 0.0);
		Double mvt = sumWithTwoValue(clientCompany.getMvt(), defference);
		invoice.setStatus(Status.INWAITING);	
		if(commandLinesDto.get(0).getInvoice().getPaid() == PaymentStatus.PAID) {
			defference = minesTwoValue(defference, commandLinesDto.get(0).getInvoice().getRest());
			invoice.setStatus(Status.ACCEPTED);
		}
		if(commandLinesDto.get(0).getInvoice().getPaid() == PaymentStatus.INCOMPLETE) {
			defference = minesTwoValue(totTtc, commandLinesDto.get(0).getInvoice().getRest());
		}
		Double credit = sumWithTwoValue(clientCompany.getCredit(),defference);
		clientCompany.setCredit(credit);
		invoice.setPrix_article_tot(totHt);
		invoice.setTot_tva_invoice(totTva);
		invoice.setPrix_invoice_tot(totTtc);
		invoiceService.insert(invoice);
		inventoryService.impacteInvoice(company,commandLines);
		clientCompany.setMvt(mvt);
		if (type.equals("pdf-save-clien") ) {	
			return invoiceService.export(company,commandLines);
		}
		return null;
	}
	
	
	private void impactOnSubArticle(ArticleCompany article, Double articleQuantity) {
		for(SubArticle i : article.getSubArticle()) {
			Double childQuantity = round(i.getChildArticle().getQuantity()-i.getQuantity()*articleQuantity);
			i.getChildArticle().setQuantity(childQuantity);
			inventoryService.impactOnSubArticle(i.getChildArticle(),i.getQuantity()*articleQuantity);
		}
		
	}


	public List<CommandLineDto> getCommandLines(Long invoiceId) {
		List<CommandLine> commandLines = commandLineRepository.findAllByInvoiceId(invoiceId);
		List<CommandLineDto> commandLinesDto = new ArrayList<>();
		for(CommandLine i : commandLines) {
			CommandLineDto commandLineDto = commandLineMapper.mapToDto(i);
			commandLinesDto.add(commandLineDto);
		}
		return commandLinesDto;
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
	private Double minesTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.subtract(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}




}
