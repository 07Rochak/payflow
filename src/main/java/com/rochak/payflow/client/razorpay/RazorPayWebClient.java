package com.rochak.payflow.client.razorpay;

import com.rochak.payflow.dto.razorpay.RazorpayOrderRequest;
import com.rochak.payflow.dto.razorpay.RazorpayOrderResponse;
import com.rochak.payflow.exception.RazorpayClientException;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

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
                .onStatus(
                        status -> status.value() >=400 && status.value() <=500,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new RazorpayClientException("Razorpay request failed with HTTP" + response.statusCode().value()+": "+body, response.statusCode().value()))
                )
                .onStatus(
                        status -> status.value() >=500,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new RazorpayClientException("Razorpay server error with HTTP" + response.statusCode().value()+": "+body, response.statusCode().value()))
                )
                .bodyToMono(RazorpayOrderResponse.class)
                .onErrorMap(
                        current ->
                        {
                            while (current!=null) {
                                if(current instanceof TimeoutException || current instanceof ReadTimeoutException){
                                    return true;
                                }
                                current = current.getCause();
                            }
                            return false;
                        },
                        exception -> new RazorpayClientException("Razorpay request timed out", 0, exception)
                )
                .onErrorMap(
                        exception -> !(exception instanceof RazorpayClientException),
                        exception -> new RazorpayClientException("Failed to communicate with Razorpay", 0, exception)
                );
    }

    private boolean isTimeoutException(Throwable exception) {
        Throwable current = exception;
        while (current!=null) {
            if(current instanceof TimeoutException || current instanceof ReadTimeoutException){
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
