package com.shirongbao.timenest.service.limit;

import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.common.entity.RateLimitInfo;
import com.shirongbao.timenest.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-05-27
 * @description: IP限流服务
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisUtil redisUtil;

    /**
     * 检查IP是否被限流
     * @param ip IP地址
     * @param minuteLimit 分钟限制次数
     * @param hourLimit 小时限制次数
     * @return true表示允许访问，false表示被限流
     */
    public boolean isAllowed(String ip, int minuteLimit, int hourLimit) {
        String minuteKey = redisUtil.buildKey(RedisConstant.RATE_LIMIT_MINUTE, ip);
        String hourKey = redisUtil.buildKey(RedisConstant.RATE_LIMIT_HOUR, ip);

        // 检查分钟级限制
        if (!checkLimit(minuteKey, minuteLimit, 60)) {
            return false;
        }

        // 检查小时级限制
        if (!checkLimit(hourKey, hourLimit, 3600)) {
            return false;
        }

        return true;
    }

    /**
     * 检查并更新访问计数
     * @param key Redis key
     * @param limit 限制次数
     * @param expireSeconds 过期时间（秒）
     * @return true表示允许访问，false表示超出限制
     */
    private boolean checkLimit(String key, int limit, int expireSeconds) {
        try {
            // 获取当前计数
            Object countObj = redisUtil.get(key);
            int currentCount = 0;
            if (countObj != null) {
                currentCount = Integer.parseInt(countObj.toString());
            }

            if (currentCount >= limit) {
                return false;
            }

            // 原子性增加计数
            Long newCount = redisUtil.increment(key, 1);

            // 如果是第一次访问，设置过期时间
            if (newCount == 1) {
                redisUtil.expire(key, expireSeconds);
            }

            return newCount <= limit;
        } catch (Exception e) {
            // Redis异常时，允许访问（可根据需要调整策略）
            return true;
        }
    }

    /**
     * 获取剩余访问次数信息
     * @param ip IP地址
     * @param minuteLimit 分钟限制次数
     * @param hourLimit 小时限制次数
     * @return 包含剩余次数和重置时间的信息
     */
    public RateLimitInfo getRateLimitInfo(String ip, int minuteLimit, int hourLimit) {
        String minuteKey = redisUtil.buildKey(RedisConstant.RATE_LIMIT_MINUTE, ip);
        String hourKey = redisUtil.buildKey(RedisConstant.RATE_LIMIT_HOUR, ip);

        RateLimitInfo info = new RateLimitInfo();

        // 获取分钟级信息
        Object minuteCountObj = redisUtil.get(minuteKey);
        int minuteCount = minuteCountObj == null ? 0 : Integer.parseInt(minuteCountObj.toString());
        long minuteTtl = redisUtil.getTime(minuteKey);

        info.setMinuteRemaining(Math.max(0, minuteLimit - minuteCount));
        info.setMinuteResetTime(minuteTtl > 0 ? minuteTtl : 0);

        // 获取小时级信息
        Object hourCountObj = redisUtil.get(hourKey);
        int hourCount = hourCountObj == null ? 0 : Integer.parseInt(hourCountObj.toString());
        long hourTtl = redisUtil.getTime(hourKey);

        info.setHourRemaining(Math.max(0, hourLimit - hourCount));
        info.setHourResetTime(hourTtl > 0 ? hourTtl : 0);

        return info;
    }
}