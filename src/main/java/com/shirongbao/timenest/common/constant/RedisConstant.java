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

    // ========== 聊天系统相关常量 ==========

    // 所有在线用户集合
    public static final String ONLINE_USERS_SET = "chat:online:users";

    // 用户会话信息
    public static final String USER_SESSION_PREFIX = "chat:user:session:";

    // 会话到用户映射
    public static final String SESSION_USER_PREFIX = "chat:session:user:";

    // 用户心跳时间
    public static final String USER_HEARTBEAT_PREFIX = "chat:user:heartbeat:";

}
