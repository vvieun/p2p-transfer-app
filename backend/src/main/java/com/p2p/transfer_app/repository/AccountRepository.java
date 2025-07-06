package com.p2p.transfer_app.repository;

import com.p2p.transfer_app.aop.LogExecutionTime;
import com.p2p.transfer_app.exception.AccountOperationException;
import com.p2p.transfer_app.model.Account;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AccountRepository 
{

    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) 
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> 
    {
        Long id = rs.getLong("id");
        String accountNumber = rs.getString("account_number");
        Long balance = rs.getLong("balance");
        Long userId = rs.getLong("user_id");
        return new Account(id, accountNumber, balance, userId);
    };

    @LogExecutionTime
    public Account save(Account account) 
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        String sql = "INSERT INTO accounts (account_number, balance, user_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(connection -> 
        {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, account.getAccountNumber());
            ps.setLong(2, account.getBalance());
            ps.setLong(3, account.getUserId());
            return ps;
        }, keyHolder);
        
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) 
        {
            Object idValue = keyHolder.getKeys().get("id");
            if (idValue instanceof Number) 
            {
                Long id = ((Number) idValue).longValue();
                account.setId(id);
            } 
            else 
            {
                throw new RuntimeException("Не удалось получить сгенерированный ID для аккаунта");
            }
        } 
        else 
        {
            throw new RuntimeException("Не удалось получить сгенерированный ID для аккаунта");
        }
        
        return account;
    }

    @LogExecutionTime
    public Optional<Account> findByAccountNumber(String accountNumber) 
    {
        try 
        {
            String sql = "SELECT id, account_number, balance, user_id FROM accounts WHERE account_number = ?";
            Account account = jdbcTemplate.queryForObject(sql, accountRowMapper, accountNumber);
            return Optional.ofNullable(account);
        } catch (EmptyResultDataAccessException e) 
        {
            return Optional.empty();
        }
    }

    @LogExecutionTime
    public Optional<Account> findById(Long id) 
    {
        try 
        {
            String sql = "SELECT id, account_number, balance, user_id FROM accounts WHERE id = ?";
            Account account = jdbcTemplate.queryForObject(sql, accountRowMapper, id);
            return Optional.ofNullable(account);
        } catch (EmptyResultDataAccessException e) 
        {
            return Optional.empty();
        }
    }

    @LogExecutionTime
    public List<Account> findAllByUserId(Long userId) 
    {
        String sql = "SELECT id, account_number, balance, user_id FROM accounts WHERE user_id = ?";
        return jdbcTemplate.query(sql, accountRowMapper, userId);
    }

    @LogExecutionTime
    public void update(Account account) 
    {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, account.getBalance(), account.getId());
        
        if (rowsAffected == 0) 
        {
            throw new AccountOperationException("Не удалось обновить аккаунт. Возможно аккаунт уже удален.");
        }
    }

    @LogExecutionTime
    public void deleteById(Long id) 
    {
        String sql = "DELETE FROM accounts WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        
        if (rowsAffected == 0) 
        {
            throw new AccountOperationException("Не удалось удалить аккаунт, так как он уже был удален.");
        }
    }
}
