package com.rochak.payflow.exception;

public class RazorpayClientException extends RuntimeException {
    private final int statusCode;
    public RazorpayClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RazorpayClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
