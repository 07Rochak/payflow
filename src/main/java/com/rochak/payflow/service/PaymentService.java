package com.rochak.payflow.service;

import com.rochak.payflow.dto.order.CreateOrderRequestDTO;
import com.rochak.payflow.dto.order.CreateOrderResponseDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;

public interface PaymentService {
    CreateOrderResponseDTO createOrder(CreateOrderRequestDTO request);
    void verifyPayment(String email, PaymentVerificationRequestDTO request);
}
