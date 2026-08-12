package com.rochak.payflow;

import com.rochak.payflow.client.razorpay.RazorPayWebClient;
import com.rochak.payflow.dto.razorpay.RazorpayOrderRequest;
import com.rochak.payflow.exception.RazorpayClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RazorpayClientRetryTest {
    private HttpServer server;
    private AtomicInteger requestCount;

    @BeforeEach
    void setUp() throws IOException {
        requestCount = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 9091),0);
        server.start();
    }

    @AfterEach
    void tearDown(){
        server.stop(0);
    }

    @Test
    void shouldRetryOnServerErrorAndEventuallySucceed() throws IOException{
        server.createContext("/v1/orders", exchange -> {
            int attempt = requestCount.incrementAndGet();
            if(attempt<=2){
                String response = """
                        {
                            "error": {
                                "code": "SERVER_ERROR",
                                "description": "Temporary server failure"
                            }
                        }
                        """;
                exchange.sendResponseHeaders(500, response.length());

                try(OutputStream outputStream = exchange.getResponseBody()){
                    outputStream.write(response.getBytes());
                }
                return;
            }
            String response = """
             {
                 "id": "order_TEST123",
                 "entity": "order",
                 "amount": 10000,
                 "amount_paid": 0,
                 "amount_due": 10000,
                 "currency": "INR",
                 "receipt": "TEST_RECEIPT",
                 "status": "created"
             }
             """;

            exchange.getResponseHeaders()
                    .set("Content-Type", "application/json");

            byte[] responseBytes = response.getBytes();

            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream outputStream =
                         exchange.getResponseBody()) {

                outputStream.write(responseBytes);
            }
        });

        RazorPayWebClient client = createClient();
        RazorpayOrderRequest request =
                RazorpayOrderRequest.builder()
                        .amount(10000)
                        .currency("INR")
                        .receipt("TEST_RECEIPT")
                        .build();

        var response = client
                .createOrder(request)
                .block();

        assertNotNull(response);

        assertEquals(
                3,
                requestCount.get(),
                "Expected initial request + 2 retries"
        );
    }

    @Test
    void shouldNotRetryOnClientError()
            throws IOException {

        server.createContext("/v1/orders", exchange -> {

            requestCount.incrementAndGet();

            String response = """
                    {
                        "error": {
                            "code": "BAD_REQUEST_ERROR",
                            "description": "Invalid request"
                        }
                    }
                    """;

            exchange.sendResponseHeaders(400, response.length());

            try (OutputStream outputStream =
                         exchange.getResponseBody()) {

                outputStream.write(response.getBytes());
            }
        });

        RazorPayWebClient client = createClient();

        RazorpayOrderRequest request =
                RazorpayOrderRequest.builder()
                        .amount(10000)
                        .currency("INR")
                        .receipt("TEST_RECEIPT")
                        .build();

        RazorpayClientException exception =
                assertThrows(
                        RazorpayClientException.class,
                        () -> client
                                .createOrder(request)
                                .block()
                );

        assertEquals(
                400,
                exception.getStatusCode()
        );

        assertEquals(
                1,
                requestCount.get(),
                "400 errors must not be retried"
        );
    }


    private RazorPayWebClient createClient(){
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:9091")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return new RazorPayWebClient(webClient);
    }
}
