package com.example.meta.store.PointsPayment.Service;

import org.springframework.stereotype.Service;

import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.PointsPayment.Entity.PaymentForAymen;
import com.example.meta.store.PointsPayment.Entity.PointsPayment;
import com.example.meta.store.PointsPayment.Repository.PaymentForAymenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Service
@Transactional
public class PaymentForAymentService extends BaseService<PaymentForAymen, Long> {

	private final PaymentForAymenRepository paymentForAymenRepository;
	
	public void insertPayment(PointsPayment pointPayment) {
		Double getenespece =  (pointPayment.getAmount()*3.0);
		PaymentForAymen paymentForAymen = new PaymentForAymen();
		paymentForAymen.setPointpayment(pointPayment);
		paymentForAymen.setGiveenespeces(null);
		paymentForAymen.setGetenespeces(getenespece);
		paymentForAymen.setStatus(false);
		paymentForAymenRepository.save(paymentForAymen);
	}
	
}
