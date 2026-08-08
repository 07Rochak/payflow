package com.rochak.payflow.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.rochak.payflow.dto.payment.CreatePaymentRequestDTO;
import com.rochak.payflow.dto.payment.CreatePaymentResponseDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;
import com.rochak.payflow.entity.Payment;
import com.rochak.payflow.entity.PaymentFailureReason;
import com.rochak.payflow.entity.PaymentStatus;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.exception.*;
import com.rochak.payflow.repository.PaymentRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.PaymentFailureService;
import com.rochak.payflow.service.PaymentService;
import com.rochak.payflow.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentFailureService paymentFailureService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    public CreatePaymentResponseDTO createPayment(CreatePaymentRequestDTO request) {
        try{
            String receipt = "PAY_"+ UUID.randomUUID();
            JSONObject options = new JSONObject();
            options.put("amount", (int)(request.getAmount()*100));
            options.put("currency", "INR");
            options.put("receipt", receipt);
            log.info("Creating order with values: Amount: {}, Currency: {}, Receipt: {}", (int)(request.getAmount()*100),
                    "INR",
                    receipt);

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
                    .receiptId(receipt)
                    .build();

            paymentRepository.save(payment);
            log.info("Created payment for User: {} Order ID: {}", user.getId(), order.get("id"));

            return CreatePaymentResponseDTO
                    .builder()
                    .orderId(order.get("id"))
                    .amount(order.get("amount"))
                    .currency(order.get("currency"))
                    .build();
        } catch (RazorpayException e) {
            throw new PaymentCreationException("Failed to create Razorpay Order",e);
        }
    }

    @Override
    @Transactional
    public void verifyPayment(String email, PaymentVerificationRequestDTO request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Payment not found")
                );
        try{
            JSONObject options = new JSONObject();

            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            log.info("Verifying Payment. Order ID: {}, Payment ID: {}. Signature: {}", request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());
            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if(!isValid){
                markPaymentFailed(payment, PaymentFailureReason.INVALID_SIGNATURE);
                paymentRepository.save(payment);
                throw new PaymentVerificationException(
                        "Invalid payment signature"
                );
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(
                            ()-> new ResourceNotFoundException("User not found")
                    );

            if(payment.getStatus() == PaymentStatus.SUCCESS){
                throw new PaymentAlreadyProcessedException("Payment already processed");
            }

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

//            AddMoneyRequestDTO dto = new AddMoneyRequestDTO();
//            dto.setAmount(payment.getAmount());

            log.info("Adding money to wallet. UserId: {}, amount: {}", payment.getUser().getId(), payment.getAmount());
//            walletService.addMoney(payment.getUser().getId(), dto);
            walletService.creditWallet(payment.getUser().getId(), payment.getAmount(), "Razorpay Wallet top-up", payment.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setFailureReason(PaymentFailureReason.NONE);
            payment.setVerifiedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            log.info("Payment verified. Order ID: {}, Payment ID: {}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            paymentRepository.save(payment);
        } catch (RazorpayException e){
            log.error(e.getStackTrace().toString());
            paymentFailureService.markPaymentFailed(payment, PaymentFailureReason.RAZORPAY_API_ERROR);
            throw new RuntimeException("Payment verification failed", e);
        }catch (WalletLimitExceededException ex){
            markPaymentFailed(
                    payment, PaymentFailureReason.WALLET_LIMIT_EXCEEDED
            );

            paymentRepository.save(payment);
            log.warn(
                    "Wallet limit exceeded. OrderId={} CurrentBalance={} AttemptedAmount={} MaxBalance={}",
                    payment.getRazorpayOrderId(),
                    ex.getCurrentBalance(),
                    ex.getAttemptedAmount(),
                    ex.getMaxBalance()
            );

            throw ex;
        }
        catch (Exception e){
            log.error(e.getStackTrace().toString());
            paymentFailureService.markPaymentFailed(payment, PaymentFailureReason.UNKNOWN);
            throw new RuntimeException("Payment failed", e);
        }

    }

    private void markPaymentFailed(Payment payment, PaymentFailureReason reason){
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setUpdatedAt(LocalDateTime.now());
        log.warn(
                "Payment marked FAILED. OrderId={} Reason={}",
                payment.getRazorpayOrderId(),
                reason
        );
    }
}
