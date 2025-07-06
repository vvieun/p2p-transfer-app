package com.p2p.transfer_app.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountCreationRequestTest {

    @Test
    void testAccountCreationRequestGettersAndSetters() {
        AccountCreationRequest request = new AccountCreationRequest();
        
        request.setUserId(1L);
        request.setInitialBalance(1000L);
        
        assertEquals(1L, request.getUserId());
        assertEquals(1000L, request.getInitialBalance());
    }
    
    @Test
    void testAccountCreationRequestEqualsAndHashCode() {
        AccountCreationRequest request1 = new AccountCreationRequest();
        request1.setUserId(1L);
        request1.setInitialBalance(1000L);
        
        AccountCreationRequest request2 = new AccountCreationRequest();
        request2.setUserId(1L);
        request2.setInitialBalance(1000L);
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testAccountCreationRequestToString() {
        AccountCreationRequest request = new AccountCreationRequest();
        request.setUserId(1L);
        request.setInitialBalance(1000L);
        
        String toString = request.toString();
        
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("1000"));
    }
} 