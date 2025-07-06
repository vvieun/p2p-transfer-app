package com.p2p.transfer_app.service;

import com.p2p.transfer_app.exception.AuthenticationException;
import com.p2p.transfer_app.model.User;
import com.p2p.transfer_app.repository.UserRepository;
import com.p2p.transfer_app.aop.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserService 
{

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) 
    {
        this.userRepository = userRepository;
    }

    @LogExecutionTime
    public User registerUser(String username, String password) 
    {
        log.info("Попытка зарегистрировать нового пользователя с именем: {}", username);

        if (username == null || username.isBlank() || password == null || password.isBlank()) 
        {
            log.warn("Регистрация не удалась для username '{}': username или password пустые", username);
            throw new IllegalArgumentException("Имя пользователя и пароль не могут быть пустыми");
        }

        if (userRepository.findByUsername(username).isPresent()) 
        {
            log.warn("Регистрация не удалась: username '{}' уже существует", username);
            throw new IllegalStateException("Пользователь с именем '" + username + "' уже существует");
        }

        String passwordHash = password;
        User newUser = new User(null, username, passwordHash);
        userRepository.save(newUser);

        log.info("Пользователь '{}' зарегистрирован успешно", username);
        return newUser;
    }
    
    @LogExecutionTime
    public User authenticateUser(String username, String password) 
    {
        log.info("Попытка аутентификации пользователя: {}", username);

        if (username == null || username.isBlank() || password == null || password.isBlank()) 
        {
            log.warn("Аутентификация не удалась для username '{}': username или password пустые", username);
            throw new IllegalArgumentException("Имя пользователя и пароль не могут быть пустыми");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) 
        {
            log.warn("Аутентификация не удалась: пользователь '{}' не найден", username);
            throw new AuthenticationException("Неверное имя пользователя или пароль.");
        }
        
        User user = userOpt.get();
        
        if (!Objects.equals(password, user.getPasswordHash())) 
        {
            log.warn("Аутентификация не удалась: неверный пароль для пользователя '{}'", username);
            throw new AuthenticationException("Неверное имя пользователя или пароль.");
        }
        
        log.info("Пользователь '{}' аутентифицирован успешно", username);
        return user;
    }
    
    public User getUserById(Long userId) 
    {
        return userRepository.findById(userId)
                .orElseThrow(() -> 
                {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new RuntimeException("Пользователь не найден");
                });
    }
}
