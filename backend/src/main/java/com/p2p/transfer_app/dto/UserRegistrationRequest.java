package com.p2p.transfer_app.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest 
{
    private String username;
    private String password;
}