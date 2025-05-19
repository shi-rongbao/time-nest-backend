package com.shirongbao.timecapsule.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description:
 */
@Component
@RequiredArgsConstructor
public class CapsuleStrategyFactory implements InitializingBean {

    private final List<CapsuleStrategy> capsuleStrategyList;

    private final Map<Integer, CapsuleStrategy> capsuleStrategyMap = new HashMap<>();

    public CapsuleStrategy getStrategy(int code) {
        return capsuleStrategyMap.get(code);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (CapsuleStrategy capsuleStrategy : capsuleStrategyList) {
            capsuleStrategyMap.put(capsuleStrategy.getCode(), capsuleStrategy);
        }
    }
}
