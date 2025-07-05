package com.shirongbao.timenest.strategy.wx;


import com.shirongbao.timenest.common.enums.MsgTypeEnum;

import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-04-27
 * @description:
 */
public interface MsgTypeStrategy {

    MsgTypeEnum getMsgType();

    // 根据不同的消息类型返回不同的内容
    String getReturnContent(Map<String, String> requestBodyMap);
}
