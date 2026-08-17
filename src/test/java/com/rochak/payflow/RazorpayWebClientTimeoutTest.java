package com.rochak.payflow;

import com.rochak.payflow.client.razorpay.RazorPayWebClient;
import com.rochak.payflow.dto.razorpay.RazorpayOrderRequest;
import com.rochak.payflow.exception.RazorpayClientException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.Exceptions;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class RazorpayWebClientTimeoutTest {

    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {

        server = HttpServer.create(new InetSocketAddress("localhost", 9090), 0);
        server.createContext("/v1/orders", exchange -> {
            try{
                Thread.sleep(10_000);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        });
        server.start();
    }

    @AfterEach
    void tearDown(){
        server.stop(0);
    }

    @Test
    void shouldThrowRetryExhaustedExceptionWhenRequestTimesOut() {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:9090")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        RazorPayWebClient razorPayWebClient =
                new RazorPayWebClient(webClient);

        RazorpayOrderRequest request = RazorpayOrderRequest.builder()
                .amount(10000)
                .currency("INR")
                .receipt("TEST_RECEIPT")
                .build();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> razorPayWebClient.createOrder(request).block()
        );

        assertTrue(
                Exceptions.isRetryExhausted(exception)
        );

        assertInstanceOf(
                RazorpayClientException.class,
                exception.getCause()
        );

        assertEquals(
                "Razorpay request timed out",
                exception.getCause().getMessage()
        );
    }

}
