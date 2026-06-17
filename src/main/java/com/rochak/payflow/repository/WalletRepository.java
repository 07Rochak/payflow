package com.rochak.payflow.repository;

import com.rochak.payflow.entity.Wallet;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUser_Id(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT w
    FROM Wallet w
    WHERE w.user.id = :userId
    """)
    Optional<Wallet> findUserIdForUpdate(@Param("userId") Long userId);
}


