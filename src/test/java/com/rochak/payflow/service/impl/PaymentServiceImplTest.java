package com.rochak.payflow.service.impl;

import com.rochak.payflow.client.razorpay.RazorpayClient;
import com.rochak.payflow.dto.payment.CreatePaymentRequestDTO;
import com.rochak.payflow.dto.payment.CreatePaymentResponseDTO;
import com.rochak.payflow.dto.request.PaymentVerificationRequestDTO;
import com.rochak.payflow.dto.razorpay.RazorpayOrderResponse;
import com.rochak.payflow.entity.*;
import com.rochak.payflow.exception.*;
import com.rochak.payflow.repository.PaymentRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.PaymentFailureService;
import com.rochak.payflow.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.razorpay.Utils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock RazorpayClient razorpayClient;
    @Mock WalletService walletService;
    @Mock UserRepository userRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PaymentFailureService paymentFailureService;
    @InjectMocks PaymentServiceImpl paymentService;

    private User user;
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        user = new User(1L, "test@example.com", "Test User", "encoded", Role.USER);
        Field field = PaymentServiceImpl.class.getDeclaredField("keySecret");
        field.setAccessible(true);
        field.set(paymentService, "test-secret");
    }

    @Test
    void createPayment_shouldCreatePendingPayment() {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setAmount(100.0);

        RazorpayOrderResponse order = new RazorpayOrderResponse();
        order.setId("order_test");
        order.setAmount(10000);
        order.setCurrency("INR");
        order.setReceipt("receipt");

        when(razorpayClient.createOrder(any())).thenReturn(Mono.just(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        try (MockedStatic<com.rochak.payflow.security.SecurityUtils> security = mockStatic(com.rochak.payflow.security.SecurityUtils.class)) {
            security.when(com.rochak.payflow.security.SecurityUtils::getCurrentUserEmail).thenReturn(user.getEmail());

            CreatePaymentResponseDTO response = paymentService.createPayment(request);

            assertEquals(10L, response.getPaymentId());
            assertEquals("order_test", response.getOrderId());
            assertEquals(10000, response.getAmount());
            assertEquals("INR", response.getCurrency());
            assertEquals(PaymentStatus.PENDING, response.getStatus());

            verify(paymentRepository).save(argThat(p ->
                    p.getAmount().equals(100.0) &&
                    p.getStatus() == PaymentStatus.PENDING &&
                    p.getFailureReason() == PaymentFailureReason.NONE &&
                    p.getRazorpayOrderId().equals("order_test") &&
                    p.getReceiptId() != null));
        }
    }

    @Test
    void createPayment_shouldPropagateRazorpayClientException() {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setAmount(100.0);
        RazorpayClientException exception = new RazorpayClientException("Razorpay failed", 500);
        when(razorpayClient.createOrder(any())).thenReturn(Mono.error(exception));

        assertSame(exception, assertThrows(RazorpayClientException.class,
                () -> paymentService.createPayment(request)));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void createPayment_shouldThrowWhenUserNotFound() {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setAmount(100.0);

        RazorpayOrderResponse order = new RazorpayOrderResponse();
        order.setId("order_test");
        order.setAmount(10000);
        order.setCurrency("INR");

        when(razorpayClient.createOrder(any()))
                .thenReturn(Mono.just(order));

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtils> security =
                     mockStatic(SecurityUtils.class)) {

            security.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(EMAIL);

            PaymentCreationException exception = assertThrows(
                    PaymentCreationException.class,
                    () -> paymentService.createPayment(request)
            );

            assertInstanceOf(
                    ResourceNotFoundException.class,
                    exception.getCause()
            );

            verify(userRepository).findByEmail(EMAIL);
            verify(paymentRepository, never()).save(any());
        }
    }


    @Test
    void verifyPayment_shouldCreditWalletAndMarkSuccess() {
        Payment payment = Payment.builder()
                .id(10L).razorpayOrderId("order_test").amount(100.0)
                .status(PaymentStatus.PENDING).failureReason(PaymentFailureReason.NONE)
                .user(user).createdAt(LocalDateTime.now()).build();

        PaymentVerificationRequestDTO request = new PaymentVerificationRequestDTO(paymentRepository);
        request.setRazorpayOrderId("order_test");
        request.setRazorpayPaymentId("pay_test");
        request.setRazorpaySignature("signature");

        when(paymentRepository.findByRazorpayOrderId("order_test")).thenReturn(Optional.of(payment));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(), eq("test-secret"))).thenReturn(true);

            paymentService.verifyPayment(user.getEmail(), request);

            assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
            assertEquals(PaymentFailureReason.NONE, payment.getFailureReason());
            assertEquals("pay_test", payment.getRazorpayPaymentId());
            assertNotNull(payment.getVerifiedAt());
            verify(walletService).creditWallet(1L, 100.0, "Razorpay Wallet top-up", "pay_test");
            verify(paymentRepository).save(payment);
        }
    }

    @Test
    void verifyPayment_shouldMarkFailedForInvalidSignature() {
        Payment payment = Payment.builder().id(10L).razorpayOrderId("order_test").amount(100.0)
                .status(PaymentStatus.PENDING).failureReason(PaymentFailureReason.NONE).user(user).build();
        PaymentVerificationRequestDTO request = new PaymentVerificationRequestDTO(paymentRepository);
        request.setRazorpayOrderId("order_test");
        request.setRazorpayPaymentId("pay_test");
        request.setRazorpaySignature("bad");
        when(paymentRepository.findByRazorpayOrderId("order_test")).thenReturn(Optional.of(payment));

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(), eq("test-secret"))).thenReturn(false);

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> paymentService.verifyPayment(
                            EMAIL,
                            request
                    )
            );

            assertEquals("Payment failed", exception.getMessage());

            assertInstanceOf(
                    PaymentVerificationException.class,
                    exception.getCause()
            );
            verify(paymentRepository).save(payment);
            verifyNoInteractions(walletService);
        }
    }

    @Test
        void verifyPayment_shouldRejectAlreadyProcessedPayment() {
        Payment payment = Payment.builder().id(10L).razorpayOrderId("order_test").amount(100.0)
                .status(PaymentStatus.SUCCESS).user(user).build();
        PaymentVerificationRequestDTO request = new PaymentVerificationRequestDTO(paymentRepository);
        request.setRazorpayOrderId("order_test");
        request.setRazorpayPaymentId("pay_test");
        request.setRazorpaySignature("signature");
        when(paymentRepository.findByRazorpayOrderId("order_test")).thenReturn(Optional.of(payment));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(), eq("test-secret"))).thenReturn(true);
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> paymentService.verifyPayment(
                            EMAIL,
                            request
                    )
            );


            verify(walletService, never()).creditWallet(anyLong(), anyDouble(), anyString(), anyString());
        }
    }

    @Test
    void verifyPayment_shouldThrowWhenPaymentNotFound() {
        PaymentVerificationRequestDTO request = new PaymentVerificationRequestDTO(paymentRepository);
        request.setRazorpayOrderId("missing");
        request.setRazorpayPaymentId("pay");
        request.setRazorpaySignature("sig");
        when(paymentRepository.findByRazorpayOrderId("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.verifyPayment(user.getEmail(), request));
        verifyNoInteractions(walletService);
    }
}
