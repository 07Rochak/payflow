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
import reactor.util.retry.Retry;

import java.time.Duration;
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
                )
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(this::isRetryable)
                );
    }
    private boolean isRetryable(Throwable exception){
        if(!(exception instanceof RazorpayClientException razorpayClientException)){
            return false;
        }
        int statusCode = razorpayClientException.getStatusCode();

        if(statusCode == 0) { // for network and timeout failures
            return true;
        }
        return statusCode >=500 && statusCode<600; // for provider and server side failures
    }
}
