package com.p2p.transfer_app.repository;

import com.p2p.transfer_app.aop.LogExecutionTime;
import com.p2p.transfer_app.model.Transaction;
import com.p2p.transfer_app.model.enums.TransactionStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.UUID;

@Repository
public class TransactionRepository 
{

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) 
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @LogExecutionTime
    public void save(Transaction transaction) 
    {
        String sql = "INSERT INTO transactions (id, from_account_id, to_account_id, amount, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
                     
        jdbcTemplate.update(sql,
                transaction.getId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                Timestamp.from(transaction.getCreatedAt().toInstant()));
    }
    
    @LogExecutionTime
    public boolean hasTransactions(Long accountId) 
    {
        String sql = "SELECT COUNT(*) FROM transactions WHERE from_account_id = ? OR to_account_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountId, accountId);
        return count != null && count > 0;
    }
}