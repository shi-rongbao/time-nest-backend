package com.shirongbao.timenest.strategy.like;

import com.shirongbao.timenest.pojo.entity.TimeNestLikeCounts;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 点赞策略接口
 */
public interface LikeStrategy {

    // 获取点赞类型code
    Integer getCode();

    // 操作点赞
    void operateLikes(Long nestId, long currentUserId);

    // 获取点赞数，如果不存在，返回-1
    Long getLikeCount(TimeNestLikeCounts timeNestLikeCounts);
}
