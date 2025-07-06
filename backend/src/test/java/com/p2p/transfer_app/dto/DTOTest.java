package com.p2p.transfer_app.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DTOTest {

    @Test
    void accountCreationRequest_ShouldSetAndGetValues() {
        AccountCreationRequest request = new AccountCreationRequest();
        
        request.setUserId(1L);
        request.setInitialBalance(1000L);
        
        assertEquals(1L, request.getUserId());
        assertEquals(1000L, request.getInitialBalance());
    }

    @Test
    void transferRequest_ShouldSetAndGetValues() {
        TransferRequest request = new TransferRequest();
        
        request.setFromAccountNumber("ACC123");
        request.setToAccountNumber("ACC456");
        request.setAmount(500L);
        
        assertEquals("ACC123", request.getFromAccountNumber());
        assertEquals("ACC456", request.getToAccountNumber());
        assertEquals(500L, request.getAmount());
    }

    @Test
    void userRegistrationRequest_ShouldSetAndGetValues() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        
        request.setUsername("testuser");
        request.setPassword("password123");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void userLoginRequest_ShouldSetAndGetValues() {
        UserLoginRequest request = new UserLoginRequest();
        
        request.setUsername("testuser");
        request.setPassword("password123");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void userLoginResponse_ShouldSetAndGetValues() {
        UserLoginResponse response = new UserLoginResponse();
        
        response.setUserId(1L);
        response.setUsername("testuser");
        
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
    }
} 