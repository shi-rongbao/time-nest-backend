package com.shirongbao.timenest.strategy.wx;

import com.shirongbao.timenest.common.enums.MsgTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-04-27
 * @description:
 */
@Component
@Slf4j
public class EventMsgStrategy implements MsgTypeStrategy {

    private static final String EVENT_SUBSCRIBE = "subscribe";

    private static final String RESP_CONTENT = "欢迎关注【拾光纪】";

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.EVENT;
    }

    @Override
    public String getReturnContent(Map<String, String> requestBodyMap) {
        String event = requestBodyMap.get("Event");
        return EVENT_SUBSCRIBE.equals(event) ? RESP_CONTENT : "";
    }

}
