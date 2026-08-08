package com.rochak.payflow.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RazorpayWebClientConfig {
    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.base-url:https://api.razorpay.com}")
    private String baseUrl;

    @Bean
    public WebClient razorpayWebClient(){
        return WebClient.builder().baseUrl(baseUrl)
                .defaultHeaders(headers ->
                        headers.setBasicAuth(keyId, keySecret))
                .build();
    }
}
