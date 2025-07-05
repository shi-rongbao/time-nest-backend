package com.shirongbao.timenest.websocket;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @date: 2025-07-05
 * @description: 扫码登录 WebSocket 处理器
 */
@Component
@Slf4j
public class LoginWebSocketHandler extends TextWebSocketHandler {


    /**
     * 用于存储所有客户端连接的容器
     * Key: sceneId (场景ID，唯一标识一个登录场景)
     * Value: WebSocketSession (一个客户端的连接会话)
     * <p>
     * 使用ConcurrentHashMap来保证线程安全
     */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 当连接成功建立后被调用
     *
     * @param session 当前客户端的连接会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. 从会话的属性中，获取我们在拦截器里放入的sceneId
        String sceneId = (String) session.getAttributes().get("sceneId");

        if (StringUtils.isNotBlank(sceneId)) {
            // 2. 将会话存入Map中，键为sceneId
            SESSIONS.put(sceneId, session);
            log.info("【WebSocket】场景[{}]连接成功，当前在线人数：{}", sceneId, SESSIONS.size());
        } else {
            // 如果没有sceneId，直接关闭连接
            session.close();
            log.warn("【WebSocket】因缺少sceneId，连接被关闭");
        }
    }

    /**
     * 当连接关闭后被调用
     *
     * @param session 当前客户端的连接会话
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 1. 同样从会话属性中获取sceneId
        String sceneId = (String) session.getAttributes().get("sceneId");

        if (StringUtils.isNotBlank(sceneId)) {
            // 2. 从Map中移除该会话
            SESSIONS.remove(sceneId);
            log.info("【WebSocket】场景[{}]连接关闭，当前在线人数：{}", sceneId, SESSIONS.size());
        }
    }

    /**
     * 发生传输错误时被调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("【WebSocket】连接出现异常", exception);
        // 如果会话仍然打开，则关闭它
        if (session.isOpen()) {
            session.close();
        }
        // 从Map中移除会话
        String sceneId = (String) session.getAttributes().get("sceneId");
        if (StringUtils.isNotBlank(sceneId)) {
            SESSIONS.remove(sceneId);
        }
    }

    /**
     * 核心方法：主动给指定场景的客户端发送消息
     * <p>
     * 这个方法会被我们的业务代码（例如处理微信消息的Service）调用。
     *
     * @param sceneId 场景ID
     * @param message 要发送给客户端的文本消息 (通常是包含token的JSON字符串)
     */
    public void sendMessageToClient(String sceneId, String message) {
        WebSocketSession session = SESSIONS.get(sceneId);

        // 判断会话是否存在且处于打开状态
        if (session != null && session.isOpen()) {
            try {
                // 发送文本消息
                session.sendMessage(new TextMessage(message));
                log.info("【WebSocket】向场景[{}]发送消息：{}", sceneId, message);
            } catch (IOException e) {
                log.error("【WebSocket】向场景[{}]发送消息失败", sceneId, e);
            }
        } else {
            log.warn("【WebSocket】尝试向场景[{}]发送消息，但连接不存在或已关闭", sceneId);
        }
    }
}
