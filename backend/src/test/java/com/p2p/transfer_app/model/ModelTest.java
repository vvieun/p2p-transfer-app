package com.p2p.transfer_app.model;

import com.p2p.transfer_app.model.enums.TransactionStatus;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    void user_ShouldSetAndGetValues() {
        User user = new User();
        
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword");
        
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpassword", user.getPasswordHash());
    }

    @Test
    void account_ShouldSetAndGetValues() {
        Account account = new Account();
        
        account.setId(1L);
        account.setAccountNumber("ACC123");
        account.setBalance(1000L);
        account.setUserId(1L);
        
        assertEquals(1L, account.getId());
        assertEquals("ACC123", account.getAccountNumber());
        assertEquals(1000L, account.getBalance());
        assertEquals(1L, account.getUserId());
    }

    @Test
    void account_WithConstructor_ShouldSetValues() {
        Account account = new Account(1L, "ACC123", 1000L, 1L);
        
        assertEquals(1L, account.getId());
        assertEquals("ACC123", account.getAccountNumber());
        assertEquals(1000L, account.getBalance());
        assertEquals(1L, account.getUserId());
    }

    @Test
    void transaction_ShouldSetAndGetValues() {
        Transaction transaction = new Transaction();
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        transaction.setId(id);
        transaction.setFromAccountId(1L);
        transaction.setToAccountId(2L);
        transaction.setAmount(500L);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(now);
        
        assertEquals(id, transaction.getId());
        assertEquals(1L, transaction.getFromAccountId());
        assertEquals(2L, transaction.getToAccountId());
        assertEquals(500L, transaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void transaction_WithBuilder_ShouldSetValues() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        Transaction transaction = Transaction.builder()
                .id(id)
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(500L)
                .status(TransactionStatus.PENDING)
                .createdAt(now)
                .build();
        
        assertEquals(id, transaction.getId());
        assertEquals(1L, transaction.getFromAccountId());
        assertEquals(2L, transaction.getToAccountId());
        assertEquals(500L, transaction.getAmount());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void transactionStatus_ShouldHaveCorrectValues() {
        assertEquals("PENDING", TransactionStatus.PENDING.name());
        assertEquals("COMPLETED", TransactionStatus.COMPLETED.name());
        assertEquals("FAILED", TransactionStatus.FAILED.name());
    }
} 