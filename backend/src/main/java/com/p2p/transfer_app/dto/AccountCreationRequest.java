package com.p2p.transfer_app.dto;

import lombok.Data;

@Data
public class AccountCreationRequest 
{
    private Long userId;
    private Long initialBalance;
}
