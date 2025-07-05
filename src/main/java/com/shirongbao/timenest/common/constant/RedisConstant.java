package com.shirongbao.timenest.common.constant;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: redis常量前缀统一管理类
 */
public final class RedisConstant {

    private RedisConstant() {
        // 防止实例化
    }

    // ========== 邮箱验证相关常量 ==========
    /** 邮箱验证码前缀 */
    public static final String EMAIL_PREFIX = "verify:email";

    /** 邮箱最近一次发送验证码时间前缀 */
    public static final String LAST_SEND_TIME = "last:sent:time:email";

    // ========== 用户缓存相关常量 ==========
    /** 用户基础信息缓存前缀 */
    public static final String USER_CACHE_PREFIX = "user:cache";

    // ========== 限流相关常量 ==========
    /** IP限流 - 分钟级前缀 */
    public static final String RATE_LIMIT_MINUTE = "rate:limit:minute";

    /** IP限流 - 小时级前缀 */
    public static final String RATE_LIMIT_HOUR = "rate:limit:hour";

    // ========== 聊天系统相关常量 ==========
    /** WebSocket 在线用户集合 */
    public static final String ONLINE_USERS_SET = "chat:websocket:online_users";

    /** 用户 WebSocket 会话信息（Hash） */
    public static final String USER_SESSION_PREFIX = "chat:websocket:session:";

    /** 会话到用户映射 */
    public static final String SESSION_USER_PREFIX = "chat:session:user:";

    /** 用户 WebSocket 心跳记录（String） */
    public static final String USER_HEARTBEAT_PREFIX = "chat:websocket:heartbeat:";

    // ========== 微信登录相关常量 ==========
    /** 微信登录验证码前缀 */
    public static final String WX_LOGIN_VERIFY_CODE_PREFIX = "wx:login:verify:code:";

}
