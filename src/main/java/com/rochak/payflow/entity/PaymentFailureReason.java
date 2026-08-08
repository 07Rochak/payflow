package com.rochak.payflow.entity;

public enum PaymentFailureReason {
    NONE,
    INVALID_SIGNATURE,
    WALLET_CREDIT_FAILED,
    WALLET_LIMIT_EXCEEDED,
    TRANSACTION_CREATION_FAILED,
    RAZORPAY_API_ERROR,
    DATABASE_ERROR,
    UNKNOWN
}
