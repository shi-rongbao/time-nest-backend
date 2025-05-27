package com.shirongbao.timenest.anno;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流的key前缀
     */
    String key() default "rate_limit";

    /**
     * 是否启用IP限流
     */
    boolean enableIpLimit() default true;

    /**
     * 分钟级限制次数
     */
    int minuteLimit() default 20;

    /**
     * 小时级限制次数
     */
    int hourLimit() default 500;

}