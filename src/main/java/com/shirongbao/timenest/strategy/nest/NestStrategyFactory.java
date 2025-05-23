package com.shirongbao.timenest.strategy.nest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: nest策略工厂类
 */
@Component
@RequiredArgsConstructor
public class NestStrategyFactory implements InitializingBean {

    private final List<NestStrategy> nestStrategyList;

    private final Map<Integer, NestStrategy> nestStrategyMap = new HashMap<>();

        public NestStrategy getStrategy(int code) {
            return nestStrategyMap.get(code);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (NestStrategy nestStrategy : nestStrategyList) {
            nestStrategyMap.put(nestStrategy.getCode(), nestStrategy);
        }
    }
}
