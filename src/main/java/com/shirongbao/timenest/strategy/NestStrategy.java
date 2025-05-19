package com.shirongbao.timenest.strategy;

import com.shirongbao.timenest.pojo.entity.TimeNest;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: nest策略接口
 */
public interface NestStrategy {

    int getCode();

    void unlockNest(TimeNest timeNest);
}
