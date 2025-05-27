package com.shirongbao.timenest.common.constant;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: redis常量前缀统一管理类
 */
public class RedisConstant {

    // 邮箱验证码前缀
    public static final String EMAIL_PREFIX = "verify:email";

    // 邮箱最近一次发送验证码时间前缀
    public static final String LAST_SEND_TIME = "last:sent:time:email";

    // 用户基础信息缓存前缀
    public static final String USER_CACHE_PREFIX = "user:cache";

    // ========== 限流相关常量 ==========
    // IP限流 - 分钟级前缀
    public static final String RATE_LIMIT_MINUTE = "rate:limit:minute";

    // IP限流 - 小时级前缀
    public static final String RATE_LIMIT_HOUR = "rate:limit:hour";

}
