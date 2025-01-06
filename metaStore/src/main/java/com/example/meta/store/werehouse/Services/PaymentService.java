package com.example.meta.store.werehouse.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.NotPermissonException;
import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.PointsPayment.Repository.PaymentForProvidersRepository;
import com.example.meta.store.werehouse.Controllers.PaymentController;
import com.example.meta.store.werehouse.Dtos.PaymentDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Invoice;
import com.example.meta.store.werehouse.Entities.Payment;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.PaymentMode;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Mappers.PaymentMapper;
import com.example.meta.store.werehouse.Repositories.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService extends BaseService<Payment, Long>{

	private final PaymentRepository paymentRepository;
	
	private final PaymentMapper paymentMapper;
	
	private final ClientService clientService;
	
	
	private final Logger logger = LoggerFactory.getLogger(PaymentService.class);


	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	public void paymentResponse(Status response, Long id, Company company) {
		Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no payment"));
		if(!payment.getInvoice().getProvider().equals(company)) {
			throw new NotPermissonException("you dont have a permission");
		}
		if(response == Status.ACCEPTED) {
			if(payment.getInvoice().getClient() != null)
			clientService.paymentInpact(payment.getInvoice().getClient().getId(),payment.getInvoice().getProvider().getId(),payment.getAmount(), payment.getInvoice().getId(), AccountType.COMPANY);
			else
				clientService.paymentInpact(payment.getInvoice().getClient().getId(),payment.getInvoice().getProvider().getId(),payment.getAmount(), payment.getInvoice().getId(), AccountType.USER);	
			
		}else {
			paymentRepository.delete(payment);
		}
		payment.setStatus(response);
		
		
	}

	
	/////////////////////////////////////////////////////// future work ///////////////////////////////////////////////////
	/////////////////////////////////////////////////////// not work ///////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<PaymentDto> getAllMy(Long userId, Long companyId){
		List<Payment> payments = paymentRepository.findAllByCompanyIdOrClientId(companyId, userId);
		return mapper(payments);
	}

	public PaymentDto getMyById(Long id) {
		Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("there is no payment "));
		
		PaymentDto paymentDto = paymentMapper.mapToDto(payment);
		return paymentDto;
		
	}


	
	
	private List<PaymentDto> mapper(List<Payment> payments){
		if(payments.isEmpty()) {
			throw new RecordNotFoundException("there is no pyment ");
		}
		List<PaymentDto> paymentsDto = new ArrayList<>();
		for(Payment i : payments) {
			PaymentDto paymentDto = paymentMapper.mapToDto(i);
			paymentsDto.add(paymentDto);
		}
		return paymentsDto;
	}


	public void addCashMode(Invoice invoice, Company company) {
		Payment payment = new Payment();
		payment.setInvoice(invoice);
		payment.setAmount(invoice.getRest());
		payment.setStatus(Status.ACCEPTED);
		payment.setType(PaymentMode.CASH);
		paymentRepository.save(payment);
	}


	public Page<PaymentDto> getPaymentHystoricByInvoiceId(Long invoiceId, int page, int pageSize) {
		Sort sort = Sort.by(Sort.Direction.DESC, "lastModifiedDate");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Page<Payment> payment = paymentRepository.findAllByInvoiceId(invoiceId , pageable);
		List<PaymentDto> response = payment.stream()
				.map(paymentMapper::mapToDto)
				.toList();
		return new PageImpl<>(response, pageable, payment.getTotalElements());
	}


	
}
