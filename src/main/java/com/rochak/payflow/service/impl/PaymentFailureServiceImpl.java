package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.Payment;
import com.rochak.payflow.entity.PaymentFailureReason;
import com.rochak.payflow.entity.PaymentStatus;
import com.rochak.payflow.repository.PaymentRepository;
import com.rochak.payflow.service.PaymentFailureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentFailureServiceImpl implements PaymentFailureService {
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void markPaymentFailed(Payment payment, PaymentFailureReason reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        log.error("Payment marked failed. OrderId ={}, Reason={}", payment.getRazorpayOrderId(), reason);
    }
}
