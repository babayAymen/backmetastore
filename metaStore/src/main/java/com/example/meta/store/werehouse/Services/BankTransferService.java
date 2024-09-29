package com.example.meta.store.werehouse.Services;


import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;

import com.example.meta.store.werehouse.Dtos.BankTransferDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Entities.Payment;
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
public class BankTransferService{
	
	private final PaymentRepository paymentRepository;
	
	private final PaymentMapper paymentMapper;
	
	private final ClientService clientService;
	
	public void invoiceBankTransferPayment(Company client, BankTransferDto bankTransferDto) {
		if(bankTransferDto.getInvoice().getClient().getId() != client.getId() && bankTransferDto.getInvoice().getProvider().getId() != client.getId()) {
			throw new PermissionDeniedDataAccessException("you don't have permission to do that", null);
		}
		if(bankTransferDto.getInvoice().getPaid() != PaymentStatus.PAID && bankTransferDto.getInvoice().getStatus() == Status.ACCEPTED) {
		Payment bankTransfer = paymentMapper.mapBanktransferToPayment(bankTransferDto);
		if(bankTransferDto.getInvoice().getProvider().getId() == client.getId()) {
			bankTransfer.setStatus(Status.ACCEPTED);
			
			clientService.paymentInpact(bankTransfer.getInvoice().getClient().getId(),bankTransfer.getInvoice().getProvider().getId(),bankTransfer.getAmount(), bankTransfer.getInvoice());
		}else {		
			bankTransfer.setStatus(Status.INWAITING);
		}
		bankTransfer.setType(PaymentMode.BANKTRANSFER);
		paymentRepository.save(bankTransfer);
		}
		
	}

}
