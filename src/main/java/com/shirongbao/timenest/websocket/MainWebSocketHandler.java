package com.shirongbao.timenest.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.common.exception.BusinessException;
import com.shirongbao.timenest.pojo.bo.ChatMessagesBo;
import com.shirongbao.timenest.pojo.dto.mq.ChatMessageMqDto;
import com.shirongbao.timenest.pojo.dto.websocket.ChatMessageRequest;
import com.shirongbao.timenest.pojo.dto.websocket.WebSocketRequest;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.service.auth.UserService;
import com.shirongbao.timenest.service.chat.ChatService;
import com.shirongbao.timenest.service.mq.MQProducerService;
import com.shirongbao.timenest.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Date;
import java.util.List;
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
@RequiredArgsConstructor
public class MainWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    private final RedisUtil redisUtil;

    private final ChatService chatService;

    private final UserService userService;

    private final MQProducerService mqProducerService;

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

        try {
            // 1. 解析通用消息体
            WebSocketRequest request = objectMapper.readValue(payload, WebSocketRequest.class);
            if (request == null || StringUtils.isBlank(request.getType())) {
                log.warn("【主通道】收到来自用户[{}]的无效消息（格式错误）：{}", userId, payload);
                return;
            }

            // 2. 根据消息类型进行分发
            switch (request.getType()) {
                case "heartbeat":
                    handleHeartbeat(session);
                    break;
                case "chat_message":
                    handleChatMessage(session, request.getData());
                    break;
                default:
                    log.warn("【主通道】收到来自用户[{}]的未知消息类型：{}", userId, request.getType());
            }
        } catch (JsonProcessingException e) {
            log.error("【主通道】解析用户[{}]的消息失败, 消息: {}, 异常: {}", userId, payload, e.getMessage());
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (ObjectUtils.isEmpty(userId)) {
            return;
        }
        log.info("【主通道】收到用户[{}]的心跳包", userId);

        String key = redisUtil.buildKey(RedisConstant.USER_HEARTBEAT_PREFIX, userId.toString());
        redisUtil.set(key, "1", 90);
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketSession session, JsonNode data) throws JsonProcessingException {
        Object objUserId = session.getAttributes().get("userId");
        Long userId = (Long) objUserId;
        try {
            // 1. 数据解析与校验
            ChatMessageRequest chatMessage = objectMapper.treeToValue(data, ChatMessageRequest.class);
            Long sessionId = chatMessage.getSessionId();
            if (sessionId == null || StringUtils.isBlank(chatMessage.getContent())) {
                log.warn("无效聊天消息: {}", data.toString());
                return;
            }

            // 2. 安全检查
            if (!chatService.isUserMemberOfSession(userId, sessionId)) {
                log.error("【安全警告】用户[{}]尝试向未加入的会话[{}]发送消息！", userId, sessionId);
                return;
            }

            // 3. 实时投递
            List<Long> memberIds = chatService.getMemberIdsBySessionId(sessionId);

            // 构建广播消息Bo
            Users sender = userService.getUsersByCache(userId.toString());
            ChatMessagesBo broadcastVo = buildBroadcast(sessionId, sender, chatMessage);

            // 3c. 序列化为JSON字符串
            String broadcastJson = objectMapper.writeValueAsString(broadcastVo);

            // 3d. 过滤在线用户并推送
            // 我们需要把消息也推送给发送者自己，这样可以实现多端同步
            memberIds.stream()
                    // 筛选出在线的用户
                    .filter(SESSIONS::containsKey)
                    .forEach(memberId -> sendMessageToUser(memberId, broadcastJson));

            // 4. 异步持久化
            // 封装用于MQ的消息体
            ChatMessageMqDto mqDto = buildMqDto(chatMessage, userId, sessionId, broadcastVo.getCreatedAt());
            mqProducerService.sendChatMessageToPersist(mqDto);

        } catch (Exception e) {
            log.error("处理聊天消息时出错", e);
            throw new BusinessException("处理消息出错！");
        }
    }

    // 构建mq dto
    private ChatMessageMqDto buildMqDto(ChatMessageRequest chatMessage, Long userId, Long sessionId, Date broadcastDate) {
        ChatMessageMqDto mqDto = new ChatMessageMqDto();
        mqDto.setSenderId(userId);
        mqDto.setSessionId(sessionId);
        mqDto.setMessageType(chatMessage.getMessageType());
        mqDto.setContent(chatMessage.getContent());
        mqDto.setClientMessageId(chatMessage.getClientMessageId());
        // 使用广播时生成的服务器时间
        mqDto.setCreatedAt(broadcastDate);
        return mqDto;
    }

    // 构建广播消息
    private ChatMessagesBo buildBroadcast(Long sessionId, Users sender, ChatMessageRequest chatMessage) {
        ChatMessagesBo broadcastBo = new ChatMessagesBo();
        // 注意：此时还没有数据库的messageId，先不设置或设为null
        broadcastBo.setSessionId(sessionId);
        broadcastBo.setSenderId(sender.getId());
        broadcastBo.setSenderNickname(sender.getNickName());
        broadcastBo.setSenderAvatar(sender.getAvatarUrl());
        // 使用客户端传来的类型
        broadcastBo.setMessageType(chatMessage.getMessageType());
        broadcastBo.setContent(chatMessage.getContent());
        // 使用服务器当前时间，保证所有人都看到统一的时间
        broadcastBo.setCreatedAt(new Date());

        return broadcastBo;
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
