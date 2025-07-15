package com.shirongbao.timenest.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author: ShiRongbao
 * @date: 2025-07-09
 * @description: 全局主 WebSocket 处理器 (用于/ws/user主通道)
 * 职责：管理所有已登录用户的长连接，负责消息的接收和推送（如：聊天、通知）。
 */
@Component
@Slf4j
public class MainWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储所有已登录用户的连接
     * Key: userId (用户ID, Sa-Token返回的是Object类型，通常为Long或String)
     * Value: WebSocketSession (该用户的连接会话)
     */
    private static final Map<Object, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话属性中获取在拦截器中放入的用户ID
        Object userId = session.getAttributes().get("userId");
        // 将会话存入Map中，键为userId
        SESSIONS.put(userId, session);
        log.info("【主通道】用户[{}]连接成功，当前总在线人数：{}", userId, SESSIONS.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Object userId = session.getAttributes().get("userId");
        // 客户端发来的消息体 (JSON字符串)
        String payload = message.getPayload();
        log.info("【主通道】收到用户[{}]的消息: {}", userId, payload);

        // TODO: 在这里实现消息的分发逻辑
        // 1. 将payload解析为我们定义的JSON对象
        // 2. 根据JSON对象中的 "type" 字段进行switch-case处理
        //    case "heartbeat": 更新心跳时间
        //    case "chat_message": 转发聊天消息
        //    ...
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object userId = session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("【主通道】用户[{}]连接关闭，当前总在线人数：{}", userId, SESSIONS.size());
        }
    }

    // 省略handleTransportError，其逻辑与afterConnectionClosed类似

    /**
     * 核心推送方法：由其他业务Service调用，向指定用户推送消息
     * @param userId  目标用户的ID
     * @param message 要发送给客户端的、符合我们定义的JSON格式的文本消息
     */
    public void sendMessageToUser(Object userId, String message) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("【主通道】向用户[{}]推送消息：{}", userId, message);
            } catch (IOException e) {
                log.error("【主通道】向用户[{}]推送消息失败", userId, e);
            }
        } else {
            // 用户不在线，可以选择：
            // 1. 忽略
            // 2. 将消息存为离线消息
            // 3. 通过MQ发送给推送服务，进行APP Push
            log.warn("【主通道】尝试向用户[{}]推送消息，但用户不在线", userId);
        }
    }
}
