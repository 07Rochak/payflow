package com.rochak.payflow.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "payflow.wallet.limits")
@Getter
@Setter
public class WalletLimitConfig {
    private Double maxBalance;
    private Double dailyTransferLimit;
    private Double dailyWithdrawalLimit;
}
