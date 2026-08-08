package com.rochak.payflow.exception;

public class WalletLimitExceededException extends RuntimeException{
    private final Double currentBalance;
    private final Double attemptedAmount;
    private final Double maxBalance;
    public WalletLimitExceededException(String message, Double currentBalance, Double attemptedAmount, Double maxBalance) {
        super(message);
        this.currentBalance = currentBalance;
        this.attemptedAmount = attemptedAmount;
        this.maxBalance=maxBalance;
    }

    public Double getAttemptedAmount() {
        return attemptedAmount;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public Double getMaxBalance() {
        return maxBalance;
    }
}
