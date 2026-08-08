package com.rochak.payflow.controller;

import com.rochak.payflow.dto.payment.CreatePaymentRequestDTO;
import com.rochak.payflow.dto.payment.CreatePaymentResponseDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;
import com.rochak.payflow.security.SecurityUtils;
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
    public ResponseEntity<CreatePaymentResponseDTO> createOrder(@RequestBody @Valid CreatePaymentRequestDTO request){
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody @Valid PaymentVerificationRequestDTO request){
        String email = SecurityUtils.getCurrentUserEmail();

        paymentService.verifyPayment(email, request);

        return ResponseEntity.ok("Payment Verified Successfully!");
    }
}
