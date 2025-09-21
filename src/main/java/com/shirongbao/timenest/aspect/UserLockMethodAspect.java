package com.shirongbao.timenest.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.shirongbao.timenest.anno.UserLockMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author ShiRongbao
 * @create 2024/9/21
 * @description:
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserLockMethodAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.shirongbao.timenest.anno.UserLockMethod)")
    public Object executeWithMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 拿到目标方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method tobeLockedMethod = methodSignature.getMethod();
        // 拿到注解
        UserLockMethod userLockMethodAnno = tobeLockedMethod.getAnnotation(UserLockMethod.class);

        // 拿到userId
        Long userId = StpUtil.getLoginIdAsLong();

        // 拿到注解参数
        long timeout = userLockMethodAnno.timeout();
        TimeUnit unit = userLockMethodAnno.unit();
        String key = userLockMethodAnno.key();
        if (StringUtils.isBlank(key)){
            log.error("key is empty!");
            throw new RuntimeException("key is empty");
        }
        String lockKey = key + userId;
        long expirationTime = userLockMethodAnno.expirationTime();
        log.info("用户尝试获取锁次数：lockKey: {}", lockKey);
        // 正常加锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁
            boolean isLocked = lock.tryLock(timeout, expirationTime, unit);
            if (isLocked) {
                log.info("lockKey: {} acquired the lock", lockKey);
                // 执行业务逻辑
                return joinPoint.proceed();
            } else {
                log.info("lockKey: {} failed to acquire the lock", lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Thread interrupted: {}", e.getMessage());
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("lockKey: {} released the lock", lockKey);
            }
        }
        // 加锁失败则不执行目标方法
        return null;
    }

}
