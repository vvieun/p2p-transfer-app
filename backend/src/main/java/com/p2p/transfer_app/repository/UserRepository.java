package com.p2p.transfer_app.repository;

import com.p2p.transfer_app.aop.LogExecutionTime;
import com.p2p.transfer_app.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserRepository 
{

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) 
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> 
    {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        return new User(id, username, passwordHash);
    };
    
    @LogExecutionTime
    public User save(User user) 
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        jdbcTemplate.update(connection -> 
        {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            return ps;
        }, keyHolder);
        
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) 
        {
            Object idValue = keyHolder.getKeys().get("id");
            if (idValue instanceof Number) 
            {
                Long id = ((Number) idValue).longValue();
                user.setId(id);
            } 
            else 
            {
                throw new RuntimeException("Не удалось получить сгенерированный ID для пользователя");
            }
        } 
        else 
        {
            throw new RuntimeException("Не удалось получить сгенерированный ID для пользователя");
        }
        
        return user;
    }

    @LogExecutionTime
    public Optional<User> findByUsername(String username) 
    {
        try 
        {
            String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) 
        {
            return Optional.empty();
        }
    }

    @LogExecutionTime
    public Optional<User> findById(Long userId) 
    {
        try 
        {
            String sql = "SELECT id, username, password_hash FROM users WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) 
        {
            return Optional.empty();
        }
    }
}