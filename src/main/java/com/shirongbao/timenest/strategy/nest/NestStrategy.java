package com.shirongbao.timenest.strategy.nest;

import com.shirongbao.timenest.pojo.entity.TimeNest;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: nest策略接口
 */
public interface NestStrategy {

    // 获取nest类型
    int getCode();

    // 解锁拾光纪条目
    void unlockTimeNest(TimeNest timeNest);

    // 创建拾光纪条目
    TimeNest createTimeNest(TimeNest timeNest);
}
