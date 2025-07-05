package com.shirongbao.timenest.strategy.nest;

import com.shirongbao.timenest.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 拾光纪策略工厂类
 */
@Component
@RequiredArgsConstructor
public class NestStrategyFactory implements InitializingBean {

    private final List<NestStrategy> nestStrategyList;

    private final Map<Integer, NestStrategy> nestStrategyMap = new HashMap<>();

    /**
     * 根据类型码获取策略实现
     *
     * @param code 策略类型码
     * @return 策略实现
     * @throws BusinessException 当策略不存在时抛出
     */
    public NestStrategy getStrategy(int code) {
        NestStrategy strategy = nestStrategyMap.get(code);
        if (strategy == null) {
            throw new BusinessException("不支持的拾光纪类型: " + code);
        }
        return strategy;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (NestStrategy nestStrategy : nestStrategyList) {
            nestStrategyMap.put(nestStrategy.getCode(), nestStrategy);
        }
    }
}
