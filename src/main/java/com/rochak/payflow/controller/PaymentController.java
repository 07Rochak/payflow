package com.rochak.payflow.controller;

import com.rochak.payflow.dto.payment.CreatePaymentRequestDTO;
import com.rochak.payflow.dto.payment.CreatePaymentResponseDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Payment order creation and Razorpay payment verification.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay payment order", description = "Creates a PayFlow payment and corresponding Razorpay order. The returned order details are used by the frontend to initialize Razorpay Checkout.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment order created", content = @Content(schema = @Schema(implementation = CreatePaymentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or payment/order creation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    public ResponseEntity<CreatePaymentResponseDTO> createOrder(@RequestBody @Valid CreatePaymentRequestDTO request){
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment", description = "Verifies the Razorpay payment signature and completes PayFlow payment processing for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid signature, payment already processed or payment verification failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<String> verifyPayment(@RequestBody @Valid PaymentVerificationRequestDTO request){
        String email = SecurityUtils.getCurrentUserEmail();

        paymentService.verifyPayment(email, request);

        return ResponseEntity.ok("Payment Verified Successfully!");
    }
}
