package com.shirongbao.timenest.strategy.like;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 创建点赞策略工厂
 */
@Component
@RequiredArgsConstructor
public class LikeStrategyFactory implements InitializingBean {

    private final List<LikeStrategy> likeStrategyList;

    private final Map<Integer, LikeStrategy> likeStrategyMap = new HashMap<>();

    public LikeStrategy getLikeStrategy(Integer likeTypeCode) {
        return likeStrategyMap.get(likeTypeCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (LikeStrategy likeStrategy : likeStrategyList) {
            likeStrategyMap.put(likeStrategy.getCode(), likeStrategy);
        }
    }

}
