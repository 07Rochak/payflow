package com.rochak.payflow.integration;

import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.Transaction;
import com.rochak.payflow.entity.Wallet;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.dto.request.TransferRequestDTO;
import com.rochak.payflow.repository.TransactionRepository;
import com.rochak.payflow.repository.UserRepository;
import com.rochak.payflow.repository.UserSessionRepository;
import com.rochak.payflow.repository.WalletRepository;
import com.rochak.payflow.service.SessionService;
import com.rochak.payflow.service.WalletService;
import com.rochak.payflow.session.RedisKeys;
import com.rochak.payflow.session.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PostgresRedisIntegrationTest extends AbstractIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired UserSessionRepository userSessionRepository;
    @Autowired SessionService sessionService;
    @Autowired WalletService walletService;
    @Autowired RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clearRedis() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void postgres_shouldPersistUserAndWallet() {
        String suffix = UUID.randomUUID().toString();
        User user = userRepository.save(
                new User(null, "db-" + suffix + "@test.com", "db-" + suffix, "encoded", Role.USER)
        );

        Wallet wallet = walletRepository.save(
                new Wallet(null, 250.0, user, false)
        );

        assertNotNull(user.getId());
        assertNotNull(wallet.getId());
        assertEquals(250.0, walletRepository.findByUser_Id(user.getId()).orElseThrow().getBalance());
    }

    @Test
    void redisAndPostgres_shouldPersistAndIndexSession() {
        String suffix = UUID.randomUUID().toString();
        User user = userRepository.save(
                new User(null, "session-" + suffix + "@test.com", "session-" + suffix, "encoded", Role.USER)
        );

        UserSession session = sessionService.createSession(user, "Chrome", "127.0.0.1");

        assertNotNull(userSessionRepository.findById(session.getSessionId()).orElse(null));

        Set<String> sessionIds = redisTemplate.opsForSet().members(
                RedisKeys.userSessions(user.getId())
        );

        assertNotNull(sessionIds);
        assertTrue(sessionIds.contains(session.getSessionId()));
    }

    @Test
    void postgres_shouldExecuteWalletTransferAndCreateLedgerTransaction() {
        String suffix = UUID.randomUUID().toString();

        User sender = userRepository.save(
                new User(null, "sender-" + suffix + "@test.com", "sender-" + suffix, "encoded", Role.USER)
        );
        User receiver = userRepository.save(
                new User(null, "receiver-" + suffix + "@test.com", "receiver-" + suffix, "encoded", Role.USER)
        );

        walletRepository.save(new Wallet(null, 500.0, sender, false));
        walletRepository.save(new Wallet(null, 100.0, receiver, false));

        TransferRequestDTO request = new TransferRequestDTO();
        request.setToUserId(receiver.getId());
        request.setAmount(75.0);

        walletService.transferMoney(sender.getEmail(), request);

        Wallet senderWallet = walletRepository.findByUser_Id(sender.getId()).orElseThrow();
        Wallet receiverWallet = walletRepository.findByUser_Id(receiver.getId()).orElseThrow();

        assertEquals(425.0, senderWallet.getBalance());
        assertEquals(175.0, receiverWallet.getBalance());

        Optional<Transaction> transaction = transactionRepository.findAll()
                .stream()
                .filter(t -> t.getSenderWallet().getId().equals(senderWallet.getId()))
                .filter(t -> t.getReceiverWallet().getId().equals(receiverWallet.getId()))
                .filter(t -> t.getAmount().equals(75.0))
                .findFirst();

        assertTrue(transaction.isPresent());
    }
}
