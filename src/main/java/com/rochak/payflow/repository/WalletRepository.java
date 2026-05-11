package com.rochak.payflow.repository;

import com.rochak.payflow.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUser_Id(Long userId);
}


