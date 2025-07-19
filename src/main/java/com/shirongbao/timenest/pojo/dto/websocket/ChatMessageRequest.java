package com.shirongbao.timenest.pojo.dto.websocket;

import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 聊天请求消息体
 */
@Data
public class ChatMessageRequest {

    // 会话id
    private Long sessionId;

    // 消息类型
    private Integer messageType;

    // 消息内容
    private String content;

    // 客户端消息id
    private String clientMessageId;

}
