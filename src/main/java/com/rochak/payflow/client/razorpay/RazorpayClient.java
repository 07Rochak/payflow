package com.rochak.payflow.client.razorpay;

import com.rochak.payflow.dto.razorpay.RazorpayOrderRequest;
import com.rochak.payflow.dto.razorpay.RazorpayOrderResponse;
import reactor.core.publisher.Mono;

public interface RazorpayClient {
    Mono<RazorpayOrderResponse> createOrder(RazorpayOrderRequest request);
}
