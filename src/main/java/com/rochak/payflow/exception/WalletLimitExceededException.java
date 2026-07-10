package com.rochak.payflow.exception;

public class WalletLimitExceededException extends RuntimeException{
    public WalletLimitExceededException(String message) {
        super(message);
    }
}
