package com.rochak.payflow.exception;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException(String message) {
        super(message);
    }
}
