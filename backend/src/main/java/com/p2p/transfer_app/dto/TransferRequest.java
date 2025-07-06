package com.p2p.transfer_app.dto;

import lombok.Data;

@Data
public class TransferRequest 
{
    private String fromAccountNumber;
    private String toAccountNumber;
    private Long amount;
}