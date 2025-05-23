package com.shirongbao.timenest.strategy.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知策略工厂
 */
@Component
@RequiredArgsConstructor
public class NotificationStrategyFactory implements InitializingBean {

    private final List<NotificationStrategy> notificationStrategies;

    private final Map<Integer, NotificationStrategy> notificationStrategyMap = new HashMap<>();

    public NotificationStrategy getStrategy(int code) {
        return notificationStrategyMap.get(code);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (NotificationStrategy notificationStrategy : notificationStrategies) {
            notificationStrategyMap.put(notificationStrategy.getCode(), notificationStrategy);
        }
    }
}
