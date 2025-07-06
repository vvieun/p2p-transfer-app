package com.p2p.transfer_app.exception;

public class AccountOperationException extends RuntimeException 
{
    public AccountOperationException(String message) 
    {
        super(message);
    }
} 