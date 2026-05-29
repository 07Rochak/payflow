package com.rochak.payflow.controller;

import com.rochak.payflow.dto.order.CreateOrderRequestDTO;
import com.rochak.payflow.dto.order.CreateOrderResponseDTO;
import com.rochak.payflow.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponseDTO> createOrder(@RequestBody @Valid CreateOrderRequestDTO request){
        return ResponseEntity.ok(paymentService.createOrder(request));
    }
}
