package com.rochak.payflow.service.impl;

import com.rochak.payflow.entity.*;
import com.rochak.payflow.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFailureServiceImplTest {
    @Mock PaymentRepository paymentRepository;
    @InjectMocks PaymentFailureServiceImpl service;

    @Test void markPaymentFailed_shouldUpdateAndSave() {
        Payment payment = Payment.builder().razorpayOrderId("order_1").status(PaymentStatus.PENDING).build();
        service.markPaymentFailed(payment, PaymentFailureReason.INVALID_SIGNATURE);
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(PaymentFailureReason.INVALID_SIGNATURE, payment.getFailureReason());
        assertNotNull(payment.getUpdatedAt());
        verify(paymentRepository).save(payment);
    }
}
