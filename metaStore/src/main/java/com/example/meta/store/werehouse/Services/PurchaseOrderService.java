package com.example.meta.store.werehouse.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordIsAlreadyExist;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.PointsPayment.Service.PaymentForProvidersSevice;
import com.example.meta.store.werehouse.Dtos.PurchaseOrderDto;
import com.example.meta.store.werehouse.Dtos.PurchaseOrderLineDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Delivery;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Entities.OrderDelivery;
import com.example.meta.store.werehouse.Entities.PurchaseOrder;
import com.example.meta.store.werehouse.Entities.PurchaseOrderLine;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Mappers.PurchaseOrderLineMapper;
import com.example.meta.store.werehouse.Mappers.PurchaseOrderMapper;
import com.example.meta.store.werehouse.Repositories.PurchaseOrderLineRepository;
import com.example.meta.store.werehouse.Repositories.PurchaseOrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseOrderService extends BaseService<PurchaseOrder, Long> {

	private final PurchaseOrderLineRepository purchaseOrderLineRepository;
	
	private final PurchaseOrderRepository purchaseOrderRepository;

	private final PurchaseOrderLineMapper purchaseOrderLineMapper;

	private final PurchaseOrderMapper purchaseOrderMapper;
	
	private final DeliveryService deliveryService;

	private final OrderDeliveryService orderDeliveryService;
	
	private final InvoiceService invoiceService;
	
	private final PaymentForProvidersSevice paymentForProvidersSevice;
	
	private final ArticleService articleService;

	private final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

	
	public List<PurchaseOrderLineDto> addPurchaseOrder(List<PurchaseOrderLineDto> purchaseOrderDto, Company client, User user) {
	    Company company = new Company();
	    List<PurchaseOrderLineDto> purchaseOrdersLineDto = new ArrayList<>();
	    PurchaseOrder purchaseOrder = null;
	    Long id = null;
	    BigDecimal buye = BigDecimal.ZERO;
	    Boolean delivery = false;
	    Collections.sort(purchaseOrderDto, Comparator.comparing(dto -> dto.getArticle().getCompany().getId()));
	    for (PurchaseOrderLineDto i : purchaseOrderDto) {
	        PurchaseOrderLine purchaseOrderLine = purchaseOrderLineMapper.mapToEntity(i);
	        if (company.getId() != null && company.getId() == purchaseOrderLine.getArticle().getCompany().getId()) {
	            purchaseOrder = purchaseOrderRepository.findById(id).get();
	        } else {
	        	Long orderNumber = (long) 001;
	            purchaseOrder = new PurchaseOrder();
	            if (client == null) {
	               purchaseOrder.setPerson(user);
	            } else {
	              purchaseOrder.setClient(client);
	            }
//	            Long orderN = purchaseOrderRepository.getLastOrderNumber(client.getId(),user.getId());
//	            if(orderN != null) {
//	            	orderNumber = orderN+1;
//	            }
	            purchaseOrder.setOrderNumber(orderNumber);
	            purchaseOrder.setCompany(purchaseOrderLine.getArticle().getCompany());
	            company = purchaseOrderLine.getArticle().getCompany();
	        }
	        purchaseOrderLine.setPurchaseorder(purchaseOrder);
	        purchaseOrderLine.setStatus(Status.INWAITING);
	        BigDecimal sellingPrice = new BigDecimal(purchaseOrderLine.getArticle().getSellingPrice());
	        BigDecimal tva = new BigDecimal(purchaseOrderLine.getArticle().getArticle().getTva());
	        BigDecimal qte = new BigDecimal(purchaseOrderLine.getQuantity());
	        purchaseOrderLine.setPrixArticleTot(qte.multiply(sellingPrice).doubleValue());
	        purchaseOrderLine.setTotTva(qte.multiply(tva).doubleValue());
	        purchaseOrderRepository.save(purchaseOrder);
	        purchaseOrderLineRepository.save(purchaseOrderLine);
	        PurchaseOrderLineDto dto = purchaseOrderLineMapper.mapToDto(purchaseOrderLine);
	        purchaseOrdersLineDto.add(dto);
	        id = purchaseOrder.getId();
	        
	         buye = buye.add(qte.multiply(sellingPrice));
	         delivery = i.getDelivery();
	    }
	    BigDecimal deliveryFees = (delivery && buye.compareTo(BigDecimal.valueOf(30)) <= 0 )
	    		? BigDecimal.valueOf(3) 
	    				: BigDecimal.ZERO;
	    if(client == null) {
	    	BigDecimal newBalance = new BigDecimal(user.getBalance()).subtract(buye).subtract(deliveryFees);
	    	user.setBalance(newBalance.setScale(2, RoundingMode.HALF_UP).doubleValue());
	    }else {
	    	BigDecimal newBalance = new BigDecimal(client.getBalance()).subtract(buye).subtract(deliveryFees);
	    	client.setBalance(newBalance.setScale(2, RoundingMode.HALF_UP).doubleValue());
	    }
	    return purchaseOrdersLineDto;
	}



	public List<PurchaseOrderDto> getAllMyPerchaseOrdersNotAccepted(Company client, Long userId) {
		logger.warn(client.getId()+" client id and person id : "+userId);
		List<PurchaseOrder>	purchaseOrderLine = purchaseOrderRepository.findAllByCompanyIdOrClientIdOrUserId(client.getId(), userId, Status.INWAITING);
		
		if(purchaseOrderLine.isEmpty()) {
			throw new RecordNotFoundException("there is no order");
		}
		List<PurchaseOrderDto> purchaseOrdersDto = new ArrayList<>();
		for(PurchaseOrder i : purchaseOrderLine) {
			PurchaseOrderDto purchaseOrderLineDto = purchaseOrderMapper.mapToDto(i);
			purchaseOrdersDto.add(purchaseOrderLineDto);
		}
		logger.warn("purchase size :"+purchaseOrdersDto.size());
		return purchaseOrdersDto;
	}

	
	public PurchaseOrderDto getOrderById(Long id, Company client, User user) {
	    PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
	            .orElseThrow(() -> new RecordNotFoundException("There is no order with id: " + id));

	    if (user.equals(purchaseOrder.getPerson())) {
	        return purchaseOrderMapper.mapToDto(purchaseOrder);
	    }

	    if (client != null && (client.equals(purchaseOrder.getClient()) || client.equals(purchaseOrder.getCompany()))) {
	        return purchaseOrderMapper.mapToDto(purchaseOrder);
	    }

	    throw new RecordNotFoundException("There is no order with id: " + id);
	}



	public double OrderResponse(Long id, Status status, Company company, Boolean isAll) {
		Double companyPoints = 0.0;
		Integer arraySize = 0;
		Integer count = 0;
		Double balance = company.getBalance();
		if(isAll) {
			List<PurchaseOrderLine> purchaseOrdersLine = purchaseOrderLineRepository.findAllByPurchaseorderId(id);
			if(purchaseOrdersLine.isEmpty()) {
				throw new RecordNotFoundException("There is no order with id: " + id);
			}
			arraySize = purchaseOrdersLine.size();
			if(status == Status.ACCEPTED) {
				// add invoice for all 
				for(PurchaseOrderLine i : purchaseOrdersLine ) {
					i.setStatus(status);
		    	 companyPoints += pointForProvider(i);
		    	if(count == arraySize - 1) {
		    		 balance = doubleWithTwoValue(company.getBalance(),companyPoints);// if can i remove this line
		    		  company.setBalance(balance);
		    	}
			    paymentForProvidersSevice.insertPaymentForProvider(i); 
			    if( i.getDelivery()) {
			    	withDelivery(i);
			    }
			    count ++;
				}
			}else {
				for(PurchaseOrderLine i : purchaseOrdersLine) {
					i.setStatus(status);
					Double val = priceArticleTotal(i);
			    	if(i.getPurchaseorder().getClient() != null) {
				    	 Double clientbalance = doubleWithTwoValue(i.getPurchaseorder().getClient().getBalance(),val);
			    		i.getPurchaseorder().getClient().setBalance(clientbalance);
			    	}else {
			    		Double clientbalance = doubleWithTwoValue(i.getPurchaseorder().getPerson().getBalance(),val);
			    		i.getPurchaseorder().getPerson().setBalance(clientbalance);
			    	}
				}
			}
		}
		else {
		  PurchaseOrderLine purchaseOrderLine = purchaseOrderLineRepository.findById(id)
	            .orElseThrow(() -> new RecordNotFoundException("There is no order with id: " + id));
	    purchaseOrderLine.setStatus(status);
	    if(status == Status.ACCEPTED) {
	    // add invoice for only one
	    	Invoice invoice = invoiceService.invoiceFromAcceptOrder(company , purchaseOrderLine);
	    	 companyPoints = pointForProvider(purchaseOrderLine);
	    	 articleService.impactFromOrder(purchaseOrderLine);
	    	 balance = doubleWithTwoValue(company.getBalance(),companyPoints);
	    company.setBalance(balance);
	    purchaseOrderLine.setInvoice(invoice);
	    paymentForProvidersSevice.insertPaymentForProvider(purchaseOrderLine);
	    if( purchaseOrderLine.getDelivery()) {
	    	withDelivery(purchaseOrderLine);
	    }
	    }
	    else {
	    	Double val = priceArticleTotal(purchaseOrderLine);
	    	if(purchaseOrderLine.getPurchaseorder().getClient() != null) {
		    	Double clientbalance = doubleWithTwoValue(purchaseOrderLine.getPurchaseorder().getClient().getBalance(),val);
	    		purchaseOrderLine.getPurchaseorder().getClient().setBalance(clientbalance);
	    	}else {
	    		Double clientbalance = doubleWithTwoValue(purchaseOrderLine.getPurchaseorder().getPerson().getBalance(),val);
	    		purchaseOrderLine.getPurchaseorder().getPerson().setBalance(clientbalance);
	    	}
	    }
		}
		return balance;
	}
	
	/*
	 *add new invoice for provider with next invoice code with all calculate needs
	 *add impact on client when is company or user with all calculate needs
	*/

	private void withDelivery(PurchaseOrderLine purchaseOrderLine) {
		Delivery deliver = deliveryService.getById((long)1).getBody();
    	OrderDelivery orderDelivery = new OrderDelivery();
    	orderDelivery.setOrder(purchaseOrderLine);
    	orderDelivery.setDelivery(deliver);
    	orderDeliveryService.insert(orderDelivery);
	}
	
	
	
	
	public Double priceArticleTotal(PurchaseOrderLine purchaseOrderLine) {
	    BigDecimal sellingPrice = new BigDecimal(purchaseOrderLine.getArticle().getSellingPrice());
	    BigDecimal qte = new BigDecimal(purchaseOrderLine.getQuantity());

	    // Perform the calculation using BigDecimal
	    BigDecimal val = sellingPrice.multiply(qte);
	    logger.warn(val+" val is");
	    return val.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	public Double pointForProvider(PurchaseOrderLine purchaseOrderLine) {
	    BigDecimal sellingPrice = new BigDecimal(purchaseOrderLine.getArticle().getSellingPrice());
	    BigDecimal qte = new BigDecimal(purchaseOrderLine.getQuantity());

	    // Perform the calculation using BigDecimal
	    BigDecimal val = sellingPrice.multiply(new BigDecimal("0.9"))
	                                 .multiply(new BigDecimal("0.2"))
	                                 .multiply(qte);
	    logger.warn(val+" val is");
	    return val.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	private Double doubleWithTwoValue(Double val1 , Double val2) {
		BigDecimal val = new BigDecimal(val1);
		BigDecimal val3 = new BigDecimal(val2);
		BigDecimal val4 = val.add(val3);
	    return val4.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
//	public void cancelOrder(Long clientId, Long userId, Long id) {
//	    PurchaseOrderLine purchaseOrderLine = purchaseOrderLineRepository.findByIdAndClientIdOrUserId(id,clientId,userId)
//	    		.orElseThrow(() -> new RecordNotFoundException("there is no order with id: "+id));		
//		purchaseOrderLine.setStatus(Status.CANCELLED);
//	}



	public void UpdatePurchaseOrderLine(PurchaseOrderLineDto purchaseOrderLineDto, Long clientId,
			Long userId) {
		PurchaseOrderLine purchaseOrderLine =
				purchaseOrderLineRepository.findByIdAndClientIdOrUserId(purchaseOrderLineDto.getId(), clientId, userId)
				.orElseThrow(() -> new RecordNotFoundException("there is no order with id: "+purchaseOrderLineDto.getId()));
		if(purchaseOrderLine.getStatus() == Status.INWAITING) {	
			purchaseOrderLine = purchaseOrderLineMapper.mapToEntity(purchaseOrderLineDto);
			purchaseOrderLineRepository.save(purchaseOrderLine);
		}else {
			throw new RecordIsAlreadyExist("you can not do that because the order is already "+purchaseOrderLine.getStatus());
		}
	}



	public List<PurchaseOrderLineDto> getAllPurchaseOrderLinesByPurchaseOrderId(Long id) {
		List<PurchaseOrderLine> purchaseOrderLines = purchaseOrderLineRepository.findAllByPurchaseorderId(id);
		if(purchaseOrderLines.isEmpty()) {
			throw new RecordNotFoundException("there is no order");
		}
		List<PurchaseOrderLineDto> purchaseOrderLinesDto = new ArrayList<>();
		for(PurchaseOrderLine i : purchaseOrderLines) {
			PurchaseOrderLineDto purchaseOrderLineDto = purchaseOrderLineMapper.mapToDto(i);
			purchaseOrderLinesDto.add(purchaseOrderLineDto);
		}
		logger.warn(purchaseOrderLinesDto.size()+" size purchase order lines");
		return purchaseOrderLinesDto;
	}



	public List<PurchaseOrderLineDto> getAllMyPurchaseOrderLinesByCompanyId(Long companyId, Long personId) {
		List<PurchaseOrderLine> purchaseOrderLines = purchaseOrderLineRepository.findAllByCompanyIdOrClientIdOrPclientId(companyId, personId);
		if(purchaseOrderLines.isEmpty()) {
			throw new RecordNotFoundException("there is no order yet "+companyId);
		}
		List<PurchaseOrderLineDto> purchaseOrderLinesDto = new ArrayList<>();
		for(PurchaseOrderLine i : purchaseOrderLines) {
			PurchaseOrderLineDto purchaseOrderLineDto = purchaseOrderLineMapper.mapToDto(i);
			purchaseOrderLinesDto.add(purchaseOrderLineDto);
		}
		logger.warn("size dto order : "+purchaseOrderLinesDto.size());
		logger.warn("company id : "+companyId+" client id : "+companyId+" person id : "+personId);
		return purchaseOrderLinesDto;
	}


	private List<PurchaseOrderLineDto> mapToPurchaseOrderLineDto(List<PurchaseOrderLine> purchaseOrdersLine){

		List<PurchaseOrderLineDto> purchaseOrderLineDtos = new ArrayList<>();
		for(PurchaseOrderLine i : purchaseOrdersLine) {
			PurchaseOrderLineDto purchaseOrderLineDto = purchaseOrderLineMapper.mapToDto(i);
			purchaseOrderLineDtos.add(purchaseOrderLineDto);
		}
		return purchaseOrderLineDtos;
	}

	public List<PurchaseOrderLineDto> getAllMyOrdersNotAcceptedAsProvider(Long id, int page, int pageSize) {
		Sort sort = Sort.by(Sort.Order.desc("purchaseorder.person"), Sort.Order.desc("lastModifiedDate"));
		Pageable pageable  = PageRequest.of(page, pageSize, sort);
		Page<PurchaseOrderLine> purchaseOrderLines = purchaseOrderLineRepository.findAllNotAcceptedAsProvider(id, Status.INWAITING, pageable);
		List<PurchaseOrderLineDto> purchaseOrderLinesDto = new ArrayList<>();
		
		for(PurchaseOrderLine i : purchaseOrderLines) {
			PurchaseOrderLineDto dto = purchaseOrderLineMapper.mapToDto(i);
			purchaseOrderLinesDto.add(dto);
		}
		logger.warn("order line size : "+purchaseOrderLines.getNumberOfElements()+" id "+id);
		return purchaseOrderLinesDto;
	}



	public List<PurchaseOrderLineDto> getAllMyOrdersNotAcceptedAsClient(Long id, int page, int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC,"lastModifiedDate");
		Pageable pageable  = PageRequest.of(page, pageSize);
		Page<PurchaseOrderLine> purchaseOrderLines = purchaseOrderLineRepository.findAllNotAcceptedAsClient(id, Status.INWAITING, pageable);
		List<PurchaseOrderLineDto> purchaseOrderLinesDto = new ArrayList<>();
		for(PurchaseOrderLine i : purchaseOrderLines) {
			PurchaseOrderLineDto dto = purchaseOrderLineMapper.mapToDto(i);
			purchaseOrderLinesDto.add(dto);
		}
		return purchaseOrderLinesDto;
	}



	public List<PurchaseOrderLineDto> getAllPurchaseOrdersLineByOrderId(Long id, int page , int pageSize) {
		List<PurchaseOrderLine> purchaseOrderLine = purchaseOrderLineRepository.findAllByPurchaseorderId(id);
		List<PurchaseOrderLineDto> purchaseOrderLineDtos = new ArrayList<>();
		for(PurchaseOrderLine i : purchaseOrderLine) {
			PurchaseOrderLineDto dto = purchaseOrderLineMapper.mapToDto(i);
			purchaseOrderLineDtos.add(dto);
		}
		return purchaseOrderLineDtos;
	}



	public List<PurchaseOrderLineDto> getAllPurchaseOrderLinesByInvoice(Long invoiceId, Long companyId, Long userId, int page,	int pageSize ) {
		Pageable pageable = PageRequest.of(page, pageSize);
		List<PurchaseOrderLineDto> purchaseOrderLineDto = new ArrayList<>();
		if(companyId != null) {
		Page<PurchaseOrderLine>	purchaseOrdersLine = purchaseOrderLineRepository.findAllByInvoiceIdAndCompanyId(invoiceId, companyId, pageable);
		purchaseOrderLineDto = mapToPurchaseOrderLineDto(purchaseOrdersLine.getContent());
		}
		if(userId != null) {
			Page<PurchaseOrderLine> purchaseOrdersLine = purchaseOrderLineRepository.findByInvoiceIdAndPersonId(invoiceId, userId, pageable);
			purchaseOrderLineDto = mapToPurchaseOrderLineDto(purchaseOrdersLine.getContent());
		}
		logger.warn("purchase order line size :" +purchaseOrderLineDto.size());
		return purchaseOrderLineDto;
		
	}



	
}
