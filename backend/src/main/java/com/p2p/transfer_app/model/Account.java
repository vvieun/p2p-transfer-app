package com.p2p.transfer_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("accounts")
public class Account 
{
    @Id
    private Long id;

    private String accountNumber;

    private Long balance;

    private Long userId;
}
