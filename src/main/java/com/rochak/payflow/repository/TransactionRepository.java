package com.rochak.payflow.repository;

import com.rochak.payflow.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderWallet_User_IdOrReceiverWallet_User_Id(Long senderWalletId, Long recieverWalletId);
}
