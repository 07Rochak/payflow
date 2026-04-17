package com.rochak.payflow.mapper;

import com.rochak.payflow.dto.response.WalletResponseDTO;
import com.rochak.payflow.entity.Wallet;

public class WalletMapper {
    public static WalletResponseDTO mapToResponse(Wallet wallet){
        return WalletResponseDTO.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUser().getId())
                .balance(wallet.getBalance())
                .build();
    }
}
