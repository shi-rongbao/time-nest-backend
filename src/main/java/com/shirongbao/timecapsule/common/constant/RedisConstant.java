package com.shirongbao.timecapsule.common.constant;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description:
 */
public class RedisConstant {

    // 邮箱验证码前缀
    public static final String EMAIL_PREFIX = "verify:email";

    // 邮箱最近一次发送验证码时间前缀
    public static final String LAST_SEND_TIME = "last:sent:time:email";

    // 用户基础信息缓存前缀
    public static final String USER_CACHE_PREFIX = "user:cache";

}
