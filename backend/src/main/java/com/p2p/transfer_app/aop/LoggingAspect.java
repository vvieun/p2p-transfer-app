package com.p2p.transfer_app.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect 
{
    private static final ThreadLocal<Long> executionTime = new ThreadLocal<>();
    
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable 
    {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executionTimeMs = endTime - startTime;

        log.info("{} выполнено за {} мс", joinPoint.getSignature(), executionTimeMs);
        
        executionTime.set(executionTimeMs);
        
        return result;
    }
    
    public static Long getLastExecutionTime() 
    {
        return executionTime.get();
    }
    
    public static void clearExecutionTime() 
    {
        executionTime.remove();
    }
}