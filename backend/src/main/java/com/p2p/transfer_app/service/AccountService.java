package com.p2p.transfer_app.service;

import com.p2p.transfer_app.dto.TransferRequest;
import com.p2p.transfer_app.exception.AccountOperationException;
import com.p2p.transfer_app.model.Account;
import com.p2p.transfer_app.model.Transaction;
import com.p2p.transfer_app.model.enums.TransactionStatus;
import com.p2p.transfer_app.repository.AccountRepository;
import com.p2p.transfer_app.repository.UserRepository;
import com.p2p.transfer_app.repository.TransactionRepository;
import com.p2p.transfer_app.aop.LogExecutionTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AccountService 
{

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final JdbcTemplate jdbcTemplate;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository, JdbcTemplate jdbcTemplate) 
    {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @LogExecutionTime
    public Account openNewAccount(Long userId, Long initialBalance) 
    {
        log.info("Открытие нового счета для user_id: {} с начальным балансом: {}", userId, initialBalance);

        userRepository.findById(userId)
                .orElseThrow(() -> 
                {
                    log.error("Не удалось создать счет: пользователь с id {} не найден", userId);
                    return new RuntimeException("Пользователь с id " + userId + " не найден");
                });

        if (initialBalance < 0) 
        {
            log.warn("Не удалось создать счет для user_id {}: начальный баланс {} отрицательный", userId, initialBalance);
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }

        String accountNumber = generateUniqueAccountNumber();
        Account newAccount = new Account(null, accountNumber, initialBalance, userId);
        Account savedAccount = accountRepository.save(newAccount);

        log.info("Успешно открыт новый счет {} для user_id: {}", accountNumber, userId);
        return savedAccount;
    }

    @LogExecutionTime
    public List<Account> getUserAccounts(Long userId) 
    {
        log.info("Получение всех счетов для user_id: {}", userId);
        return accountRepository.findAllByUserId(userId);
    }

    @Transactional
    @LogExecutionTime
    public void transferMoney(TransferRequest request) 
    {
        log.info("Перевод начат: {} от {} до {}", request.getAmount(), request.getFromAccountNumber(), request.getToAccountNumber());

        if (request.getAmount() <= 0) 
        {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }
        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new RuntimeException("Счет-источник не найден"));
        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new RuntimeException("Счет-получатель не найден"));
        if (fromAccount.getId().equals(toAccount.getId())) 
        {
            throw new IllegalArgumentException("Нельзя переводить деньги на тот же счет");
        }
        if (fromAccount.getBalance() < request.getAmount()) 
        {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("Транзакция {} создана со статусом PENDING.", transaction.getId());

        fromAccount.setBalance(fromAccount.getBalance() - request.getAmount());
        toAccount.setBalance(toAccount.getBalance() + request.getAmount());

        accountRepository.update(fromAccount);
        accountRepository.update(toAccount);
        log.info("Балансы обновлены для счетов {} и {}", fromAccount.getAccountNumber(), toAccount.getAccountNumber());

        log.info("Перевод для транзакции {} завершен успешно", transaction.getId());
    }
    
    public Account getAccountById(Long accountId) 
    {
        log.info("Получение счета с ID: {}", accountId);
        return accountRepository.findById(accountId)
                .orElseThrow(() -> 
                {
                    log.error("Счет с id {} не найден", accountId);
                    return new AccountOperationException("Счет не найден");
                });
    }
    
    public Account getAccountByNumber(String accountNumber) 
    {
        log.info("Получение счета с номером: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> 
                {
                    log.error("Счет с номером {} не найден", accountNumber);
                    return new AccountOperationException("Счет не найден");
                });
    }

    @Transactional
    @LogExecutionTime
    public void closeAccount(Long accountId) 
    {
        log.info("Закрытие счета с ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> 
                {
                    log.error("Не удалось закрыть счет: счет с id {} не найден", accountId);
                    return new RuntimeException("Счет с id " + accountId + " не найден");
                });
        
        String deleteTransactionsSql = "DELETE FROM transactions WHERE from_account_id = ? OR to_account_id = ?";
        int deletedTransactions = jdbcTemplate.update(deleteTransactionsSql, accountId, accountId);
        if (deletedTransactions > 0) 
        {
            log.info("Удалено {} транзакций, связанных с ID счета: {}", deletedTransactions, accountId);
        }
        
        accountRepository.deleteById(accountId);
        log.info("Счет {} для user_id: {} закрыт успешно", account.getAccountNumber(), account.getUserId());
    }

    private String generateUniqueAccountNumber() 
    {
        return "ACC" + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase().substring(0, 17);
    }
}