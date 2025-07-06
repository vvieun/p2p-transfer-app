package com.p2p.transfer_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> 
{
    private T data;
    private Long executionTimeMs;
    private String message;
    
    public ApiResponse(T data, Long executionTimeMs) 
    {
        this.data = data;
        this.executionTimeMs = executionTimeMs;
    }
    
    public static <T> ApiResponse<T> success(T data, Long executionTimeMs) 
    {
        return new ApiResponse<>(data, executionTimeMs);
    }
    
    public static <T> ApiResponse<T> success(T data, Long executionTimeMs, String message) 
    {
        return new ApiResponse<>(data, executionTimeMs, message);
    }
} 