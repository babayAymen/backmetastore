package com.example.meta.store.werehouse.Services;


import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;

import com.example.meta.store.werehouse.Dtos.CheckDto;
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
public class CheckService{
	
	private final PaymentRepository paymentRepository;
	
	private final PaymentMapper paymentMapper;
	
	private final ClientService clientService;
	
	public void invoiceCheckPayment(Company client, CheckDto checkDto) {
		if(checkDto.getInvoice().getClient().getId() != client.getId() && checkDto.getInvoice().getProvider().getId() != client.getId()) {
			throw new PermissionDeniedDataAccessException("you don't have permission to do that", null);
		}
		if(checkDto.getInvoice().getPaid() != PaymentStatus.PAID && checkDto.getInvoice().getStatus() == Status.ACCEPTED) {			
		Payment check = paymentMapper.mapCheckToPayment(checkDto);
		if(checkDto.getInvoice().getProvider().getId() == client.getId()) {
			check.setStatus(Status.ACCEPTED);
			if(check.getInvoice().getClient() != null)
			clientService.paymentInpact(checkDto.getInvoice().getClient().getId(),check.getInvoice().getProvider().getId(),check.getAmount(), checkDto.getInvoice().getId(), AccountType.COMPANY);
			else
				clientService.paymentInpact(checkDto.getInvoice().getClient().getId(),check.getInvoice().getProvider().getId(),check.getAmount(), checkDto.getInvoice().getId(), AccountType.USER);

		}else {
			check.setStatus(Status.INWAITING);			
		}
		check.setType(PaymentMode.CHECK);
		paymentRepository.save(check);
		}
		
	}

}
