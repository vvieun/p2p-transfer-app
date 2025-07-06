package com.p2p.transfer_app.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransferRequestTest {

    @Test
    void testTransferRequestGettersAndSetters() {
        TransferRequest request = new TransferRequest();
        
        request.setFromAccountNumber("ACC123");
        request.setToAccountNumber("ACC456");
        request.setAmount(500L);
        
        assertEquals("ACC123", request.getFromAccountNumber());
        assertEquals("ACC456", request.getToAccountNumber());
        assertEquals(500L, request.getAmount());
    }
    
    @Test
    void testTransferRequestEqualsAndHashCode() {
        TransferRequest request1 = new TransferRequest();
        request1.setFromAccountNumber("ACC123");
        request1.setToAccountNumber("ACC456");
        request1.setAmount(500L);
        
        TransferRequest request2 = new TransferRequest();
        request2.setFromAccountNumber("ACC123");
        request2.setToAccountNumber("ACC456");
        request2.setAmount(500L);
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testTransferRequestToString() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC123");
        request.setToAccountNumber("ACC456");
        request.setAmount(500L);
        
        String toString = request.toString();
        
        assertTrue(toString.contains("ACC123"));
        assertTrue(toString.contains("ACC456"));
        assertTrue(toString.contains("500"));
    }
} 