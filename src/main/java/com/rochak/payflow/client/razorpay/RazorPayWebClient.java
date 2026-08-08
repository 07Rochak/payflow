package com.rochak.payflow.client.razorpay;

import com.rochak.payflow.dto.razorpay.RazorpayOrderRequest;
import com.rochak.payflow.dto.razorpay.RazorpayOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RazorPayWebClient implements RazorpayClient{
    private final WebClient razorpayWebClient;

    @Override
    public Mono<RazorpayOrderResponse> createOrder(RazorpayOrderRequest request) {
        log.info(
                "Creating Razorpay order. Amount: {}, Currency: {}, Receipt: {}",
                request.getAmount(),
                request.getCurrency(),
                request.getReceipt()
        );

        return razorpayWebClient
                .post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RazorpayOrderResponse.class);
    }
}
