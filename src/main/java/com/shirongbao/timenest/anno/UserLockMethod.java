package com.shirongbao.timenest.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author ShiRongbao
 * @create 2025/9/21
 * @description: 在方法上加锁的注解，这里使用redisson实现
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserLockMethod {

    // 超时时间，默认3
    long timeout() default 3;

    // 时间单位
    TimeUnit unit() default TimeUnit.SECONDS;

    // 加锁key
    String key();

    // 锁的有效时间
    long expirationTime() default 5;
}
