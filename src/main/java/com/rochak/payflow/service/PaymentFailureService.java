package com.rochak.payflow.service;

import com.rochak.payflow.entity.Payment;
import com.rochak.payflow.entity.PaymentFailureReason;

public interface PaymentFailureService {

    public void markPaymentFailed(Payment payment, PaymentFailureReason reason);
}
