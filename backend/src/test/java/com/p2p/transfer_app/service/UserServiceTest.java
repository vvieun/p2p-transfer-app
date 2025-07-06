package com.p2p.transfer_app.service;

import com.p2p.transfer_app.exception.AuthenticationException;
import com.p2p.transfer_app.model.User;
import com.p2p.transfer_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void registerUser_WithValidData_ShouldCreateUser() {
        String username = "testuser";
        String password = "password123";
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setPasswordHash("password123");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(username, password);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNotNull(result);
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnUser() {
        String username = "testuser";
        String password = "password123";
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("password123");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.authenticateUser(username, password);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void authenticateUser_WithInvalidUsername_ShouldThrowException() {
        String username = "nonexistent";
        String password = "password123";
        
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Exception exception = assertThrows(AuthenticationException.class, () -> {
            userService.authenticateUser(username, password);
        });
        
        assertTrue(exception.getMessage().contains("Неверное имя пользователя или пароль"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void authenticateUser_WithInvalidPassword_ShouldThrowException() {
        String username = "testuser";
        String password = "wrongpassword";
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("correctpassword");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(AuthenticationException.class, () -> {
            userService.authenticateUser(username, password);
        });
        
        assertTrue(exception.getMessage().contains("Неверное имя пользователя или пароль"));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNonExistentId_ShouldThrowException() {
        Long userId = 999L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(userId);
        });
        
        assertTrue(exception.getMessage().contains("Пользователь не найден"));
        verify(userRepository).findById(userId);
    }
} 