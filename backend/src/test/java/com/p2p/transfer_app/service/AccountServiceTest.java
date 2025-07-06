package com.p2p.transfer_app.service;

import com.p2p.transfer_app.dto.TransferRequest;
import com.p2p.transfer_app.model.Account;
import com.p2p.transfer_app.model.Transaction;
import com.p2p.transfer_app.model.User;
import com.p2p.transfer_app.model.enums.TransactionStatus;
import com.p2p.transfer_app.repository.AccountRepository;
import com.p2p.transfer_app.repository.TransactionRepository;
import com.p2p.transfer_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, userRepository, transactionRepository, jdbcTemplate);
    }

    @Test
    void openNewAccount_WithValidData_ShouldCreateAccount() {
        Long userId = 1L;
        Long initialBalance = 1000L;
        User user = new User();
        user.setId(userId);
        
        Account savedAccount = new Account(1L, "ACC12345", initialBalance, userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.openNewAccount(userId, initialBalance);

        assertNotNull(result);
        assertEquals(initialBalance, result.getBalance());
        assertEquals(userId, result.getUserId());
        
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void openNewAccount_WithNonExistentUser_ShouldThrowException() {
        Long userId = 999L;
        Long initialBalance = 1000L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.openNewAccount(userId, initialBalance);
        });
        
        assertTrue(exception.getMessage().contains("не найден"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void openNewAccount_WithNegativeBalance_ShouldThrowException() {
        Long userId = 1L;
        Long initialBalance = -100L;
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.openNewAccount(userId, initialBalance);
        });
        
        assertTrue(exception.getMessage().contains("отрицательным"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getUserAccounts_ShouldReturnUserAccounts() {
        Long userId = 1L;
        List<Account> expectedAccounts = Arrays.asList(
            new Account(1L, "ACC123", 1000L, userId),
            new Account(2L, "ACC456", 2000L, userId)
        );
        
        when(accountRepository.findAllByUserId(userId)).thenReturn(expectedAccounts);

        List<Account> result = accountService.getUserAccounts(userId);

        assertEquals(2, result.size());
        verify(accountRepository).findAllByUserId(userId);
    }

    @Test
    void transferMoney_WithValidRequest_ShouldTransferMoney() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        String fromAccountNumber = "ACC123";
        String toAccountNumber = "ACC456";
        Long initialFromBalance = 1000L;
        Long initialToBalance = 500L;
        Long transferAmount = 300L;
        
        Account fromAccount = new Account(fromAccountId, fromAccountNumber, initialFromBalance, 1L);
        Account toAccount = new Account(toAccountId, toAccountNumber, initialToBalance, 2L);
        
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber(fromAccountNumber);
        request.setToAccountNumber(toAccountNumber);
        request.setAmount(transferAmount);
        
        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(toAccount));

        accountService.transferMoney(request);

        assertEquals(initialFromBalance - transferAmount, fromAccount.getBalance());
        assertEquals(initialToBalance + transferAmount, toAccount.getBalance());
        
        verify(accountRepository).update(fromAccount);
        verify(accountRepository).update(toAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferMoney_WithInsufficientFunds_ShouldThrowException() {
        String fromAccountNumber = "ACC123";
        String toAccountNumber = "ACC456";
        Long fromBalance = 200L;
        Long transferAmount = 300L;
        
        Account fromAccount = new Account(1L, fromAccountNumber, fromBalance, 1L);
        Account toAccount = new Account(2L, toAccountNumber, 500L, 2L);
        
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber(fromAccountNumber);
        request.setToAccountNumber(toAccountNumber);
        request.setAmount(transferAmount);
        
        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(toAccount));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.transferMoney(request);
        });
        
        assertTrue(exception.getMessage().contains("Недостаточно средств"));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).update(any(Account.class));
    }

    @Test
    void transferMoney_ToSameAccount_ShouldThrowException() {
        String accountNumber = "ACC123";
        
        Account account = new Account(1L, accountNumber, 1000L, 1L);
        
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber(accountNumber);
        request.setToAccountNumber(accountNumber);
        request.setAmount(100L);
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.transferMoney(request);
        });
        
        assertTrue(exception.getMessage().contains("тот же счет"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferMoney_WithNonExistentSourceAccount_ShouldThrowException() {
        String fromAccountNumber = "NON_EXISTENT";
        String toAccountNumber = "ACC456";
        
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber(fromAccountNumber);
        request.setToAccountNumber(toAccountNumber);
        request.setAmount(100L);
        
        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.transferMoney(request);
        });
        
        assertTrue(exception.getMessage().contains("не найден"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void closeAccount_WithExistingAccount_ShouldDeleteAccount() {
        Long accountId = 1L;
        Account account = new Account(accountId, "ACC123", 1000L, 1L);
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.closeAccount(accountId);

        verify(accountRepository).deleteById(accountId);
    }

    @Test
    void closeAccount_WithNonExistentAccount_ShouldThrowException() {
        Long accountId = 999L;
        
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.closeAccount(accountId);
        });
        
        assertTrue(exception.getMessage().contains("не найден"));
        verify(accountRepository, never()).deleteById(anyLong());
    }
} 