package com.p2p.transfer_app.exception;

public class AuthenticationException extends RuntimeException 
{
    public AuthenticationException(String message) 
    {
        super(message);
    }
} 