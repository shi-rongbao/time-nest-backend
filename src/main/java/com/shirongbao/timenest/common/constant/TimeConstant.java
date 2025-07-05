package com.shirongbao.timenest.common.constant;

/**
 * @author: ShiRongbao
 * @date: 2025-06-27
 * @description: 时间相关常量类
 */
public final class TimeConstant {

    private TimeConstant() {
        // 防止实例化
    }

    // ========== 时间单位常量（毫秒） ==========
    /** 一秒的毫秒数 */
    public static final long SECOND_IN_MILLIS = 1000L;

    /** 一分钟的毫秒数 */
    public static final long MINUTE_IN_MILLIS = 60 * SECOND_IN_MILLIS;

    /** 一小时的毫秒数 */
    public static final long HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS;

    /** 一天的毫秒数 */
    public static final long DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS;

    /** 一周的毫秒数 */
    public static final long WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS;

    // ========== 时间单位常量（秒） ==========
    /** 一分钟的秒数 */
    public static final int MINUTE_IN_SECONDS = 60;

    /** 一小时的秒数 */
    public static final int HOUR_IN_SECONDS = 60 * MINUTE_IN_SECONDS;

    /** 一天的秒数 */
    public static final int DAY_IN_SECONDS = 24 * HOUR_IN_SECONDS;

    // ========== 业务相关时间常量 ==========
    /** WebSocket 心跳超时时间（秒） */
    public static final int WEBSOCKET_HEARTBEAT_TIMEOUT = 90;

    /** 邮箱验证码有效期（秒） */
    public static final int EMAIL_CODE_EXPIRE_TIME = 5 * MINUTE_IN_SECONDS;

    /** 邮箱验证码发送间隔（秒） */
    public static final int EMAIL_CODE_SEND_INTERVAL = MINUTE_IN_SECONDS;

}
