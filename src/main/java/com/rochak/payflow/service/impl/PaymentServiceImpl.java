package com.rochak.payflow.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.rochak.payflow.dto.order.CreateOrderRequestDTO;
import com.rochak.payflow.dto.order.CreateOrderResponseDTO;
import com.rochak.payflow.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;

    @Override
    public CreateOrderResponseDTO createOrder(CreateOrderRequestDTO request) {
        try{
            JSONObject options = new JSONObject();
            options.put("amount", (int)(request.getAmount()*100));
            options.put("currency", "INR");
            options.put("receipt", "receipt_"+System.currentTimeMillis());

            Order order = razorpayClient.orders.create(options);

            return CreateOrderResponseDTO
                    .builder()
                    .orderId(order.get("id"))
                    .amount(order.get("amount"))
                    .currency(order.get("currency"))
                    .build();
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay Order",e);
        }
    }
}
