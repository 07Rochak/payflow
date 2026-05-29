package com.rochak.payflow.service;

import com.rochak.payflow.dto.order.CreateOrderRequestDTO;
import com.rochak.payflow.dto.order.CreateOrderResponseDTO;

public interface PaymentService {
    CreateOrderResponseDTO createOrder(CreateOrderRequestDTO request);
}
