package com.p2p.transfer_app.controller;

import com.p2p.transfer_app.aop.LogExecutionTime;
import com.p2p.transfer_app.aop.LoggingAspect;
import com.p2p.transfer_app.dto.ApiResponse;
import com.p2p.transfer_app.dto.UserLoginRequest;
import com.p2p.transfer_app.dto.UserLoginResponse;
import com.p2p.transfer_app.dto.UserRegistrationRequest;
import com.p2p.transfer_app.model.User;
import com.p2p.transfer_app.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController 
{
    private final UserService userService;

    public UserController(UserService userService) 
    {
        this.userService = userService;
    }

    @GetMapping("/test")
    public String test() 
    {
        return "Hello World";
    }

    @PostMapping("/register")
    @LogExecutionTime
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody UserRegistrationRequest request) 
    {
        log.info("Получен запрос на регистрацию пользователя: {}", request.getUsername());
        try 
        {
            User user = userService.registerUser(request.getUsername(), request.getPassword());
            Long executionTime = LoggingAspect.getLastExecutionTime();
            LoggingAspect.clearExecutionTime();
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, executionTime, "Пользователь успешно зарегистрирован"));
        } catch (Exception e) 
        {
            log.error("Ошибка при регистрации пользователя {}: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/login")
    @LogExecutionTime
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@RequestBody UserLoginRequest request) 
    {
        log.info("Получен запрос на вход пользователя: {}", request.getUsername());
        try 
        {
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());
            UserLoginResponse response = new UserLoginResponse(user.getId(), user.getUsername());
            Long executionTime = LoggingAspect.getLastExecutionTime();
            LoggingAspect.clearExecutionTime();
            
            return ResponseEntity.ok(ApiResponse.success(response, executionTime, "Вход выполнен успешно"));
        } catch (Exception e) 
        {
            log.error("Ошибка при входе пользователя {}: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) 
    {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}