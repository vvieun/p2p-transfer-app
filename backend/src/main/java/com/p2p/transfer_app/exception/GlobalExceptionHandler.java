package com.p2p.transfer_app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler 
{

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) 
    {
        log.warn("Плохой запрос: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) 
    {
        log.warn("Конфликт: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) 
    {
        log.warn("Ошибка аутентификации: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountOperationException.class)
    public ResponseEntity<String> handleAccountOperationException(AccountOperationException ex) 
    {
        log.warn("Ошибка операции со счетом: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleCustomRuntimeException(RuntimeException ex) 
    {
        log.error("RuntimeException перехвачен: {}", ex.getMessage(), ex);
        
        if (ex.getMessage() != null && ex.getMessage().contains("not found")) 
        {
            log.warn("Не найден: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
        
        if (ex.getMessage() != null && 
        (
            ex.getMessage().contains("account") || 
            ex.getMessage().contains("Account") ||
            ex.getMessage().contains("user") ||
            ex.getMessage().contains("User"))) {
            log.warn("Ошибка в бизнес-логике: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        
        if (ex.getMessage() != null && ex.getMessage().contains("ID")) 
        {
            log.error("Ошибка базы данных: {}", ex.getMessage());
            return new ResponseEntity<>("Ошибка при создании записи в базе данных.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        log.error("Внутренняя ошибка сервера: ", ex);
        return new ResponseEntity<>("Произошла непредвиденная внутренняя ошибка.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}