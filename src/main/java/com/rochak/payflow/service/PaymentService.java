package com.rochak.payflow.service;

import com.rochak.payflow.dto.payment.CreatePaymentRequestDTO;
import com.rochak.payflow.dto.payment.CreatePaymentResponseDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;

public interface PaymentService {
    CreatePaymentResponseDTO createPayment(CreatePaymentRequestDTO request);
    void verifyPayment(String email, PaymentVerificationRequestDTO request);
}
