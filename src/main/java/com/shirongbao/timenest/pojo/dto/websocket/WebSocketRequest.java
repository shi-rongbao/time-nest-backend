package com.shirongbao.timenest.pojo.dto.websocket;

import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: WebSocket通用请求体
 */
@Data
public class WebSocketRequest {

    /**
     * 消息类型，用于路由到不同的处理器
     * 例如: "heartbeat", "chat_message"
     */
    private String type;

    /**
     * 消息的具体数据内容
     * 使用JsonNode可以在不确定具体类型的情况下预先解析，增加了灵活性
     */
    private JsonNode data;

}
