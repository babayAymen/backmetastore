package com.example.meta.store.werehouse.Services;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.InvoiceDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.CommandLine;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.InvoiceDetailsType;
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Mappers.InvoiceMapper;
import com.example.meta.store.werehouse.Repositories.CommandLineRepository;
import com.example.meta.store.werehouse.Repositories.InvoiceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InvoiceService extends BaseService<Invoice, Long>{


	private final InvoiceMapper invoiceMapper;
		
	private final InvoiceRepository invoiceRepository;
	
	private final CommandLineRepository commandLineRepository;
	
	private final CompanyService companyService;
	
	private final ArticleService articleService;

	private final InventoryService inventoryService;
	
	private final UserService userService;
	
	private final EnableToCommentService enableToCommentService;
	
	private final PaymentService paymentService;
	
	private final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
	
	///////////////////////////////////////////////////////////////////////// real work ////////////////////////////////////////////////////////
	public void accepted(Long invoiceId, Long clientId, AccountType type) {
		Invoice invoice = super.getById(invoiceId).getBody();
		List<CommandLine> commandLines = commandLineRepository.findAllByInvoiceId(invoiceId);
		if(type == AccountType.COMPANY) {
		articleService.impactInvoice(commandLines);
		}
		invoice.setStatus(Status.ACCEPTED);
		invoiceRepository.save(invoice);
		enableToCommentService.makeEnableToComment(invoice.getProvider(), invoice.getPerson(), invoice.getClient());
	}
	
	public Long getLastInvoice(Long companyId) {
		Optional<Invoice> invoice = invoiceRepository.lastInvoice(companyId);
		if(invoice.isEmpty()) {
			return  (long) 20230001;
		}
		return (long) (invoice.get().getCode()+1);
	}
	
	public void refused(Long invoiceId, Long clientId) {
		Invoice invoice = super.getById(invoiceId).getBody();
		List<CommandLine> commandLines = commandLineRepository.findAllByInvoiceId(invoice.getId());
		inventoryService.rejectInvoice(commandLines, invoice.getProvider().getId());
		invoice.setStatus(Status.REFUSED);
		invoiceRepository.save(invoice);
	}
	
	public List<InvoiceDto> getMyInvoiceAsProvider(Long companyId, Long userId, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<Invoice> invoices =  invoiceRepository.findAllByProviderId(companyId, pageable);
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		for(Invoice i : invoices) {
			InvoiceDto invoiceDto = invoiceMapper.mapToDto(i);
			invoicesDto.add(invoiceDto);
		}
		logger.warn("invoice size: "+invoicesDto.size());
		return invoicesDto;
	}
	
	public List<InvoiceDto> getInvoicesAsClient(Long id, AccountType type) {
		Pageable pageable = PageRequest.of(0, 1);
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		Page<Invoice> invoices = null;
		if(type == AccountType.COMPANY) {
		 invoices = invoiceRepository.findAllByClientId(id, pageable);
		}
		else {
			 invoices = invoiceRepository.findAllByPersonIdAndStatus(id, Status.ACCEPTED, pageable);
		}
		for(Invoice i : invoices) {
			InvoiceDto invoiceDto = invoiceMapper.mapToDto(i);
			invoicesDto.add(invoiceDto);
		}
		logger.warn("return size of invoice as client"+invoicesDto.size());
		return invoicesDto;
	}
	
	
	
	public List<InvoiceDto> getInvoiceNotifications( Company company,Long userId) {
		List<Invoice> invoices = new ArrayList<>();
		if(company == null) {
//			invoices = invoiceRepository.findAllByPersonId(userId);
		}else {			
			invoices = invoiceRepository.findAllByClientIdOrProviderId(company.getId());
		}
		if(invoices.isEmpty()) {
			throw new RecordNotFoundException("there is no invoice not accepted");
		}
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		for(Invoice i : invoices) {
			InvoiceDto invoiceDto = invoiceMapper.mapToDto(i);
			invoicesDto.add(invoiceDto);
		}
		return invoicesDto;
	}

	public ResponseEntity<InputStreamResource> export(Company company, List<CommandLine> commandLines) {
		ByteArrayInputStream bais = ExportInvoicePdf.invoicePdf(commandLines,company);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=invoice.pdf");
		ResponseEntity<InputStreamResource> response = ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bais));
		return response;
	}

	public Invoice addInvoice(Company company, Long clientId, AccountType clientType, InvoiceDto inv) {
		Long invoiceCode = getLastInvoice(company.getId());
		Invoice invoice = new Invoice();
		if(clientType == AccountType.COMPANY) {
		Company client = companyService.getById(clientId).getBody();
		invoice.setClient(client);
		if(client.equals(company) || client.isVirtual()) {
			invoice.setStatus(Status.ACCEPTED);			
		}else {
			invoice.setStatus(Status.INWAITING);			
		}
		}else {
			User client = userService.findById(clientId).orElseThrow(() ->  new RecordNotFoundException("this client does not exist"));
			invoice.setPerson(client);
		}
		invoice.setCode(invoiceCode);
		invoice.setProvider(company);
		invoice.setPaid(inv.getPaid());
		invoice.setType(InvoiceDetailsType.COMMAND_LINE);
//		invoice.setIsEnabledToComment(true);
		invoice.setRest(inv.getRest());
		invoiceRepository.save(invoice);
		enableToCommentService.makeEnableToComment(company, invoice.getPerson(), invoice.getProvider());
		paymentService.addCashMode(invoice,company);
		if(inv.getPaid() == PaymentStatus.PAID) {
			invoice.setRest(0.0);			
		}
		return invoice;
	}
	
	public void cancelInvoice(Company company, Long id) {
		Invoice invoice = super.getById(id).getBody();
		if(!company.equals(invoice.getProvider())) {
			throw new PermissionDeniedDataAccessException("you dont have permission", null);
		}
		List<CommandLine> commandLines = commandLineRepository.findAllByInvoiceId(invoice.getId());
		
		for(CommandLine i : commandLines) {
			ArticleCompany article = articleService.findByArticleCompanyId(i.getArticle().getId());
			article.setQuantity(article.getQuantity()+i.getQuantity());
			commandLineRepository.delete(i);
		}
		invoiceRepository.delete(invoice);
		
	}

	public void paymenInpact(Invoice invoice, Double amount) {
		Invoice invoic = invoiceRepository.findById(invoice.getId()).orElseThrow(() -> new RecordNotFoundException("invoice is not found"));
		if(invoic.getPrix_invoice_tot() > amount) {
			return;
		}
		invoic.setPaid(PaymentStatus.PAID);
	}

	public Invoice invoiceFromAcceptOrder(Company company, PurchaseOrderLine purchaseOrderLine) {
		Long lastInvoiceCode = getLastInvoice(company.getId());
		Invoice invoice = new Invoice();
		invoice.setCode(lastInvoiceCode);
		invoice.setPaid(PaymentStatus.PAID);
		invoice.setRest(0.0);
		invoice.setStatus(Status.ACCEPTED);
		invoice.setClient(purchaseOrderLine.getPurchaseorder().getClient());
		invoice.setPerson(purchaseOrderLine.getPurchaseorder().getPerson());
		invoice.setProvider(company);
		invoice.setType(InvoiceDetailsType.ORDER_LINE);
		Double priceArticleTot = multipleWithTwoValue(purchaseOrderLine.getArticle().getSellingPrice(), purchaseOrderLine.getQuantity());
		Double totTvaInvoice = multipleWithTwoValue(
				multipleWithTwoValue(purchaseOrderLine.getArticle().getSellingPrice(),purchaseOrderLine.getArticle().getArticle().getTva()/100),
				purchaseOrderLine.getQuantity());
		Double priceInvoiceTot = sumWithTwoValue(priceArticleTot, totTvaInvoice);
		invoice.setPrix_article_tot(priceArticleTot);
		invoice.setTot_tva_invoice(totTvaInvoice);
		invoice.setPrix_invoice_tot(priceInvoiceTot);
		invoiceRepository.save(invoice);
		inventoryService.impactAcceptingOrderOnInventory(purchaseOrderLine);
		enableToCommentService.makeEnableToComment(company,purchaseOrderLine.getPurchaseorder().getPerson(),purchaseOrderLine.getPurchaseorder().getClient());
		return invoice;
	}

	private Double multipleWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.multiply(val3);//gf
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	private Double sumWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.add(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public List<InvoiceDto> getAllMyInvoicesNotAcceptedAsClient(Long userId , Long companyId, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<Invoice> invoices = null;
		if(userId != null) {
			invoices = invoiceRepository.findAllByPersonIdAndStatus(userId,Status.INWAITING, pageable);
		}
		if(companyId != null) {
			invoices = invoiceRepository.findAllByClientIdAndStatus(companyId, Status.INWAITING, pageable);
		}
		if(invoices.isEmpty()) {
			throw new RecordNotFoundException("threre is no invoice not accepted");
		}
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		for(Invoice i : invoices) {
			InvoiceDto invoiceDto = invoiceMapper.mapToDto(i);
			invoicesDto.add(invoiceDto);
		}
		logger.warn("response size: "+invoicesDto.size());
		return invoicesDto;
	}

	public List<InvoiceDto> getAllMyInvoicesAsProviderAndStatus(Long companyId, PaymentStatus status, int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<Invoice> invoices = invoiceRepository.findByProviderIdAndPaid(companyId , status, pageable);
		if(invoices.isEmpty()) {
			throw new RecordNotFoundException("there is no invocie "+status);
		}
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		for(Invoice i : invoices) {
			InvoiceDto invoiceDto = invoiceMapper.mapToDto(i);
			invoicesDto.add(invoiceDto);
		}
		return invoicesDto;
	}

	public List<InvoiceDto> getAllMyInvoicesNotAcceptedAsProvider( Long companyId, Status status , int page , int pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<Invoice> invoices = invoiceRepository.findByProviderIdAndStatus(companyId , status, pageable);
		if(invoices.isEmpty()) {
			throw new RecordNotFoundException("there is no invoice not accepted as provider");
		}
		List<InvoiceDto> invoicesDto = new ArrayList<>();
		for(Invoice i : invoices) {
			InvoiceDto invoiceDto = invoiceMapper.mapToDto(i);
			invoicesDto.add(invoiceDto);
		}
		return invoicesDto;
	}





}
