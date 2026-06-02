package com.rochak.payflow.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.rochak.payflow.dto.order.CreateOrderRequestDTO;
import com.rochak.payflow.dto.order.CreateOrderResponseDTO;
import com.rochak.payflow.dto.request.AddMoneyRequestDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.ResourceNotFoundException;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.service.PaymentService;
import com.rochak.payflow.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final WalletService walletService;
    private final UserRepository userRepository;
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

    @Override
    public void verifyPayment(String email, PaymentVerificationRequestDTO request) {
        try{
            JSONObject options = new JSONObject();

            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            System.out.println(request.getRazorpayOrderId());
            System.out.println(request.getRazorpayPaymentId());
            System.out.println(request.getRazorpaySignature());
            System.out.println("key secret: "+keySecret);

            if(!isValid){
                throw new RuntimeException(
                        "Invalid payment signature"
                );
            }

            System.out.println("valid: "+isValid);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(
                            ()-> new ResourceNotFoundException("User not found")
                    );

            AddMoneyRequestDTO requestDTO = new AddMoneyRequestDTO();

            requestDTO.setAmount(request.getAmount());
            System.out.println("starting add money");
            walletService.addMoney(user.getId(), requestDTO);
            System.out.println("add money completed");
        } catch (RazorpayException e){
            System.out.println("payment verification failed");
            e.printStackTrace();
            throw new RuntimeException("Payment verification failed", e);
        }
    }
}
