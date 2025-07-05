package com.shirongbao.timenest.utils;

import com.shirongbao.timenest.common.constant.TimeConstant;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-06-27
 * @description: 时间工具类
 */
public final class TimeUtil {

    private TimeUtil() {
        // 防止实例化
    }

    /**
     * 计算距离解锁还有多少天
     *
     * @param unlockTime 解锁时间
     * @return 剩余天数（至少为1天）
     */
    public static int calculateUnlockDays(Date unlockTime) {
        if (unlockTime == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long unlockTimeMillis = unlockTime.getTime();
        
        if (unlockTimeMillis <= currentTime) {
            return 0;
        }

        // 计算剩余天数，向上取整，确保至少显示1天
        int days = (int) Math.ceil((double) (unlockTimeMillis - currentTime) / TimeConstant.DAY_IN_MILLIS);
        return Math.max(days, 1);
    }

    /**
     * 检查时间是否已过期
     *
     * @param targetTime 目标时间
     * @return true-已过期，false-未过期
     */
    public static boolean isExpired(Date targetTime) {
        if (targetTime == null) {
            return true;
        }
        return targetTime.getTime() <= System.currentTimeMillis();
    }

    /**
     * 获取指定秒数后的时间
     *
     * @param seconds 秒数
     * @return 指定秒数后的时间
     */
    public static Date getTimeAfterSeconds(int seconds) {
        return new Date(System.currentTimeMillis() + seconds * TimeConstant.SECOND_IN_MILLIS);
    }

}
