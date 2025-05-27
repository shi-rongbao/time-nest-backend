package com.shirongbao.timenest.common.entity;

import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-05-27
 * @description: 限流信息
 */
@Data
public class RateLimitInfo {

    // 分钟内剩余次数
    private int minuteRemaining;

    // 分钟限制重置时间（秒）
    private long minuteResetTime;

    // 小时内剩余次数
    private int hourRemaining;

    // 小时限制重置时间（秒）
    private long hourResetTime;

}