package com.p2p.transfer_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2p.transfer_app.dto.AccountCreationRequest;
import com.p2p.transfer_app.dto.TransferRequest;
import com.p2p.transfer_app.model.Account;
import com.p2p.transfer_app.model.User;
import com.p2p.transfer_app.service.AccountService;
import com.p2p.transfer_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAccount_ShouldReturnCreatedAccount() throws Exception {
        Long userId = 1L;
        Long initialBalance = 1000L;
        
        AccountCreationRequest request = new AccountCreationRequest();
        request.setUserId(userId);
        request.setInitialBalance(initialBalance);
        
        Account createdAccount = new Account(1L, "ACC123", initialBalance, userId);
        
        when(userService.getUserById(userId)).thenReturn(new User());
        when(accountService.openNewAccount(eq(userId), eq(initialBalance))).thenReturn(createdAccount);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.accountNumber", is("ACC123")))
                .andExpect(jsonPath("$.data.balance", is(1000)))
                .andExpect(jsonPath("$.data.userId", is(1)))
                .andExpect(jsonPath("$.message", is("Счет успешно создан")));
    }

    @Test
    void getUserAccounts_ShouldReturnAccountsList() throws Exception {
        Long userId = 1L;
        List<Account> accounts = Arrays.asList(
            new Account(1L, "ACC123", 1000L, userId),
            new Account(2L, "ACC456", 2000L, userId)
        );
        
        when(userService.getUserById(userId)).thenReturn(new User());
        when(accountService.getUserAccounts(userId)).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].accountNumber", is("ACC123")))
                .andExpect(jsonPath("$.data[1].accountNumber", is("ACC456")))
                .andExpect(jsonPath("$.message", is("Счета успешно загружены")));
    }

    @Test
    void transferMoney_ShouldReturnSuccess() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC123");
        request.setToAccountNumber("ACC456");
        request.setAmount(500L);
        
        User user = new User();
        user.setId(1L);
        Account fromAccount = new Account(1L, "ACC123", 1000L, 1L);
        
        when(accountService.getAccountByNumber("ACC123")).thenReturn(fromAccount);
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(accountService).transferMoney(request);

        mockMvc.perform(post("/api/accounts/transfer")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is("Перевод успешно выполнен")));
    }

    @Test
    void deleteAccount_ShouldReturnSuccess() throws Exception {
        Long accountId = 1L;
        
        User user = new User();
        user.setId(1L);
        Account account = new Account(1L, "ACC123", 1000L, 1L);
        
        when(accountService.getAccountById(accountId)).thenReturn(account);
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(accountService).closeAccount(accountId);

        mockMvc.perform(delete("/api/accounts/{accountId}", accountId)
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is("Счет успешно закрыт")));
    }
} 