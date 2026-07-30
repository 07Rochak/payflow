package com.rochak.payflow.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.rochak.payflow.dto.order.CreateOrderRequestDTO;
import com.rochak.payflow.dto.order.CreateOrderResponseDTO;
import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;
import com.rochak.payflow.entity.Payment;
import com.rochak.payflow.entity.PaymentStatus;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.repository.PaymentRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.PaymentService;
import com.rochak.payflow.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    public CreateOrderResponseDTO createOrder(CreateOrderRequestDTO request) {
        try{
            JSONObject options = new JSONObject();
            options.put("amount", (int)(request.getAmount()*100));
            options.put("currency", "INR");
            options.put("receipt", "receipt_"+System.currentTimeMillis());

            log.info("Creating order with values: Amount: {}, Currency: {}, Receipt: receipt_{}", (int)(request.getAmount()*100),
                    "INR",
                    System.currentTimeMillis());

            Order order = razorpayClient.orders.create(options);

            User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                    .orElseThrow(
                            ()->new ResourceNotFoundException("User not found")
                    );

            Payment payment = Payment.builder()
                    .razorpayOrderId(order.get("id"))
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);
            log.info("Created payment for User: {} Order ID: {}", user.getId(), order.get("id"));

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

    @Override
    public void verifyPayment(String email, PaymentVerificationRequestDTO request) {
        try{
            JSONObject options = new JSONObject();

            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            log.info("Verifying Payment. Order ID: {}, Payment ID: {}. Signature: {}", request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());
            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if(!isValid){
                throw new RuntimeException(
                        "Invalid payment signature"
                );
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(
                            ()-> new ResourceNotFoundException("User not found")
                    );

            Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                            .orElseThrow(
                                    ()-> new ResourceNotFoundException("Payment not found")
                            );
            if(payment.getStatus() == PaymentStatus.SUCCESS){
                throw new RuntimeException("Payment already processed");
            }

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment verified. Order ID: {}, Payment ID: {}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            paymentRepository.save(payment);

            AddMoneyRequestDTO dto = new AddMoneyRequestDTO();
            dto.setAmount(payment.getAmount());

            log.info("Adding money to wallet. UserId: {}, amount: {}", payment.getUser().getId(), payment.getAmount());
            walletService.addMoney(payment.getUser().getId(), dto);
        } catch (RazorpayException e){
            e.printStackTrace();
            throw new RuntimeException("Payment verification failed", e);
        }
    }
}
