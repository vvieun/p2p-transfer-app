package com.p2p.transfer_app.controller;

import com.p2p.transfer_app.aop.LogExecutionTime;
import com.p2p.transfer_app.aop.LoggingAspect;
import com.p2p.transfer_app.dto.AccountCreationRequest;
import com.p2p.transfer_app.dto.ApiResponse;
import com.p2p.transfer_app.dto.TransferRequest;
import com.p2p.transfer_app.model.Account;
import com.p2p.transfer_app.model.User;
import com.p2p.transfer_app.service.AccountService;
import com.p2p.transfer_app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController 
{
    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) 
    {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PostMapping
    @LogExecutionTime
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody AccountCreationRequest request) 
    {
        log.info("Получен запрос на создание счета для пользователя: {}", request.getUserId());
        try 
        {
            userService.getUserById(request.getUserId());
            Account account = accountService.openNewAccount(request.getUserId(), request.getInitialBalance());
            Long executionTime = LoggingAspect.getLastExecutionTime();
            LoggingAspect.clearExecutionTime();
            
            log.info("Счет успешно создан: {}", account.getAccountNumber());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, executionTime, "Счет успешно создан"));
        } catch (Exception e) 
        {
            log.error("Ошибка при создании счета для пользователя {}: {}", request.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/user/{userId}")
    @LogExecutionTime
    public ResponseEntity<ApiResponse<List<Account>>> getUserAccounts(@PathVariable Long userId) 
    {
        try 
        {
            userService.getUserById(userId);
            List<Account> accounts = accountService.getUserAccounts(userId);
            Long executionTime = LoggingAspect.getLastExecutionTime();
            LoggingAspect.clearExecutionTime();
            
            return ResponseEntity.ok(ApiResponse.success(accounts, executionTime, "Счета успешно загружены"));
        } catch (Exception e) 
        {
            log.error("Ошибка при получении счетов пользователя {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/transfer")
    @LogExecutionTime
    public ResponseEntity<ApiResponse<String>> transferMoney(@RequestBody TransferRequest request, @RequestParam Long userId) 
    {
        try 
        {
            Account fromAccount = accountService.getAccountByNumber(request.getFromAccountNumber());
            User owner = userService.getUserById(fromAccount.getUserId());
            
            if (!owner.getId().equals(userId)) 
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.success("Доступ запрещен", 0L));
            }
            
            accountService.transferMoney(request);
            Long executionTime = LoggingAspect.getLastExecutionTime();
            LoggingAspect.clearExecutionTime();
            
            return ResponseEntity.ok(ApiResponse.success("Перевод успешно выполнен", executionTime));
        } catch (Exception e) 
        {
            log.error("Ошибка при переводе денег: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @DeleteMapping("/{accountId}")
    @LogExecutionTime
    public ResponseEntity<ApiResponse<String>> deleteAccount(@PathVariable Long accountId, @RequestParam Long userId) 
    {
        try 
        {
            Account account = accountService.getAccountById(accountId);
            User owner = userService.getUserById(account.getUserId());
            
            if (!owner.getId().equals(userId)) 
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.success("Доступ запрещен", 0L));
            }
            
            accountService.closeAccount(accountId);
            Long executionTime = LoggingAspect.getLastExecutionTime();
            LoggingAspect.clearExecutionTime();
            
            return ResponseEntity.ok(ApiResponse.success("Счет успешно закрыт", executionTime));
        } catch (Exception e) 
        {
            log.error("Ошибка при удалении счета {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }
}