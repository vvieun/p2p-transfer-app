package com.p2p.transfer_app.model;

import com.p2p.transfer_app.model.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("transactions")
public class Transaction 
{
    @Id
    private UUID id;
    private Long fromAccountId;
    private Long toAccountId;
    private Long amount;
    private TransactionStatus status;
    private OffsetDateTime createdAt;
}