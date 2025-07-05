package com.shirongbao.timenest.strategy.wx;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-04-27
 * @description:
 */
@Component
@RequiredArgsConstructor
public class MsgTypeStrategyFactory implements InitializingBean {

    private final List<MsgTypeStrategy> msgTypeStrategyList;

    private final Map<String, MsgTypeStrategy> msgTypeStrategyMap = new HashMap<>();

    public MsgTypeStrategy getStrategy (String msgType) {
        return msgTypeStrategyMap.get(msgType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (MsgTypeStrategy msgTypeStrategy : msgTypeStrategyList) {
            msgTypeStrategyMap.put(msgTypeStrategy.getMsgType().getType(), msgTypeStrategy);
        }
    }
}
