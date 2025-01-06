package com.example.meta.store.werehouse.Services;

import java.util.Optional;

import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Dtos.BillDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Payment;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.PaymentMode;
import com.example.meta.store.werehouse.Enums.PaymentStatus;
import com.example.meta.store.werehouse.Enums.Status;
import com.example.meta.store.werehouse.Mappers.PaymentMapper;
import com.example.meta.store.werehouse.Repositories.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BillService{
	
	private final PaymentRepository paymentRepository;
	
	private final PaymentMapper paymentMapper;
	
	private final ClientService clientService;
	
	public void invoiceBillPayment(Company client, BillDto billDto) {
		if(billDto.getInvoice().getClient().getId() != client.getId() && billDto.getInvoice().getProvider().getId() != client.getId()) {
			throw new PermissionDeniedDataAccessException("you don't have permission to do that", null);
		}
		if(billDto.getInvoice().getPaid() != PaymentStatus.PAID && billDto.getInvoice().getStatus() == Status.ACCEPTED) {			
		Payment bill = paymentMapper.mapBillToPayment(billDto);
		if(billDto.getInvoice().getProvider().getId() == client.getId()) {
			bill.setStatus(Status.ACCEPTED);
			if(bill.getInvoice().getClient() != null)
			clientService.paymentInpact(billDto.getInvoice().getClient().getId(),bill.getInvoice().getProvider().getId(),bill.getAmount(), billDto.getInvoice().getId() , AccountType.COMPANY);
			else
				clientService.paymentInpact(billDto.getInvoice().getClient().getId(),bill.getInvoice().getProvider().getId(),bill.getAmount(), billDto.getInvoice().getId() , AccountType.USER);

		}else {
			bill.setStatus(Status.INWAITING);
		}
		bill.setType(PaymentMode.BILL);
		paymentRepository.save(bill);
		}
		
		
	}

}
