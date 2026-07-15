package com.rochak.payflow.repository;

import com.rochak.payflow.entity.Transaction;
import com.rochak.payflow.entity.TransactionStatus;
import com.rochak.payflow.entity.TransactionType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderWallet_User_IdOrReceiverWallet_User_Id(Long senderWalletId, Long recieverWalletId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.senderWallet.user.id = :userId
        AND t.transactionType = :transactionType
        AND t.status = :status
        AND t.createdAt >= :startOfDay
    """)
    Double getTodayTransactionAmount(
            @Param("userId") Long userId,
            @Param("transactionType") TransactionType transactionType,
            @Param("status") TransactionStatus status,
            @Param("startOfDay")LocalDateTime startOfDay
            );
}
