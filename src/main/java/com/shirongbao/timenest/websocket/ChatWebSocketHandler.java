package com.shirongbao.timenest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.timenest.common.enums.ChatMessageTypeEnum;
import com.shirongbao.timenest.common.enums.ReadStatusEnum;
import com.shirongbao.timenest.interceptor.SaTokenWebSocketInterceptor;
import com.shirongbao.timenest.pojo.entity.ChatMessage;
import com.shirongbao.timenest.service.chat.ChatMessageService;
import com.shirongbao.timenest.service.chat.UserPresenceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * @date: 2025-06-03
 * @description: 聊天websocket handler
 */
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 存放建立 websocket 连接的用户
    private final Map<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final UserPresenceService userPresenceService;

    private final ChatMessageService chatMessageService;

    // WebSocket 消息类型定义 (与前端和后端协议保持一致，可以使用固定值，不一定需要枚举类)
    // 这里采用直接的整数值，因为它们是协议层面的类型，而不是业务数据类型
    public static final int WS_TYPE_CHAT = 1;      // 聊天消息
    public static final int WS_TYPE_HEARTBEAT = 10; // 心跳消息
    public static final int WS_TYPE_READ_ACK = 20;  // 已读确认
    public static final int WS_TYPE_SYSTEM_MESSAGE = 100; // 系统消息（例如：连接成功、通知）
    public static final int WS_TYPE_ERROR_MESSAGE = 101;   // 错误消息
    public static final int WS_TYPE_WARNING_MESSAGE = 102; // 警告消息


    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            log.warn("WebSocket connection refused: userId not found in session for {}. Closing session.", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Missing user ID"));
            return;
        }

        // 处理重复连接：如果同一个用户ID已经有活跃的 WebSocketSession，关闭旧的会话
        if (onlineSessions.containsKey(userId)) {
            log.warn("User {} already has an active WebSocket session. Closing previous session and establishing new one.", userId);
            WebSocketSession oldSession = onlineSessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                try {
                    oldSession.close(CloseStatus.POLICY_VIOLATION.withReason("New connection established for the same user"));
                } catch (IOException e) {
                    log.error("Failed to close old session for user {}: {}", userId, e.getMessage());
                }
            }
        }

        // 把当前用户id和session放到map中
        onlineSessions.put(userId, session);

        // 调用 UserPresenceService 将用户标记为 WebSocket 在线
        userPresenceService.userOnline(userId);

        log.info("User {} connected. Session ID: {}", userId, session.getId());

        // 发送连接成功系统消息
        sendSystemMessageToClient(session, "Welcome " + userId + "! Connected to chat server.");

        // 用户上线时，推送其所有未读消息
        List<ChatMessage> unreadMessages = chatMessageService.getUnreadMessages(userId);
        if (!unreadMessages.isEmpty()) {
            log.info("Pushing {} unread messages to user {}.", unreadMessages.size(), userId);
            for (ChatMessage msg : unreadMessages) {
                // 将数据库中的 ChatMessage 实体转换为 WebSocket 消息格式发送
                // 这里 messageType 直接使用 ChatMessage 实体中的 Integer 类型
                sendChatMessageToClient(session, WS_TYPE_CHAT, msg.getSenderId(), msg.getReceiverId(), msg.getContent(), msg.getMessageType(), msg.getId(), msg.getSendTime());
            }
            // 可以在这里发送一个通知，告诉客户端有多少条离线消息被推送
            sendSystemMessageToClient(session, "You have " + unreadMessages.size() + " unread messages.");
        } else {
            sendSystemMessageToClient(session, "No unread messages.");
        }
    }

    /**
     * 接收到文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) {
            sendErrorMessageToClient(session, "Unauthorized user. Please reconnect with a valid user ID.");
            return;
        }

        // 收到任何消息都更新心跳
        try {
            userPresenceService.updateHeartbeat(senderId);
        } catch (Exception e) {
            log.error("Failed to update heartbeat for user {}: {}", senderId, e.getMessage());
        }

        log.debug("Received raw message from {}: {}", senderId, message.getPayload());

        // 使用内部定义的 WebSocketClientMessage 结构来解析客户端消息
        WebSocketClientMessage clientMessage;
        try {
            clientMessage = objectMapper.readValue(message.getPayload(), WebSocketClientMessage.class);
            if (clientMessage.getSenderId() == null) { // 强制服务端设置 senderId
                clientMessage.setSenderId(senderId);
            } else if (!clientMessage.getSenderId().equals(senderId)) {
                log.warn("Client {} tried to spoof senderId to {}. Corrected to {}.", senderId, clientMessage.getSenderId(), senderId);
                clientMessage.setSenderId(senderId); // 防止客户端伪造发送者ID
            }

        } catch (IOException e) {
            log.error("Failed to parse message from {}: {}, error: {}", senderId, message.getPayload(), e.getMessage());
            sendErrorMessageToClient(session, "Invalid message format.");
            return;
        }

        // 根据消息类型进行处理 (现在使用 Integer 类型)
        if (clientMessage.getType() == null) {
            log.warn("Received message with null type from user {}. Message: {}", senderId, message.getPayload());
            sendErrorMessageToClient(session, "Message type is missing.");
            return;
        }

        switch (clientMessage.getType()) {
            case WS_TYPE_CHAT:
                handleChatMessage(clientMessage);
                break;
            case WS_TYPE_HEARTBEAT:
                // 心跳消息已在前面通过 updateHeartbeat 统一处理
                log.debug("Received heartbeat from user {}", senderId);
                // 可以选择回复一个 ACK 消息，但通常不是必须的
                // sendSystemMessageToClient(session, "Heartbeat ACK");
                break;
            case WS_TYPE_READ_ACK: // 客户端发送的已读确认消息
                handleReadAcknowledgement(clientMessage);
                break;
            // TODO: 其他消息类型，例如 "get_history", "get_online_friends" 等
            default:
                log.warn("Received unknown message type: {} from user {}", clientMessage.getType(), senderId);
                sendErrorMessageToClient(session, "Unknown message type: " + clientMessage.getType());
        }
    }

    /**
     * 连接关闭后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            onlineSessions.remove(userId);
            // 调用 UserPresenceService 将用户标记为 WebSocket 离线
            userPresenceService.userOffline(userId);
            log.info("User {} disconnected. Session ID: {}, Status: {}", userId, session.getId(), status);
            // TODO: 通知该用户的好友其已下线 (需要 PUSH 机制或广播)
        } else {
            log.warn("Unknown user disconnected. Session ID: {}, Status: {}", session.getId(), status);
        }
    }

    /**
     * 传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("WebSocket transport error for user {} (Session ID: {}): {}", userId, session.getId(), exception.getMessage(), exception);
        // 可以在这里尝试关闭会话，但 Spring WebSocket 框架通常会自动处理
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR.withReason("Transport error: " + exception.getMessage()));
        }
    }

    // ---------------------- 核心业务逻辑方法 ----------------------

    /**
     * 处理客户端发送的聊天消息
     *
     * @param clientMessage 客户端消息对象
     */
    private void handleChatMessage(WebSocketClientMessage clientMessage) throws IOException {
        Long senderId = clientMessage.getSenderId();
        Long receiverId = clientMessage.getReceiverId();
        String content = clientMessage.getContent();
        Integer messageType = clientMessage.getMessageType(); // 客户端传入的消息内容类型

        if (receiverId == null) {
            log.warn("Chat message from {} has no receiver ID. Message: {}", senderId, content);
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            log.warn("Chat message from {} to {} has empty content.", senderId, receiverId);
            return;
        }

        // 1. 构建要保存到数据库的 ChatMessage 实体
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessage.setSendTime(new Date());
        chatMessage.setReadStatus(ReadStatusEnum.UNREAD.getCode());

        // 验证客户端传入的 messageType 是否合法，并设置
        try {
            // 尝试通过 code 获取枚举，如果不存在会抛异常
            ChatMessageTypeEnum chatMessageType = ChatMessageTypeEnum.getByCode(messageType != null ? messageType : ChatMessageTypeEnum.TEXT.getCode());
            chatMessage.setMessageType(chatMessageType.getCode());
        } catch (RuntimeException e) {
            log.warn("Unknown messageType '{}' from client {}. Defaulting to 'text'. Error: {}", messageType, senderId, e.getMessage());
            chatMessage.setMessageType(ChatMessageTypeEnum.TEXT.getCode()); // 默认文本
        }

        // 2. 保存消息到数据库
        boolean saved = chatMessageService.saveChatMessage(chatMessage);
        if (!saved) {
            log.error("Failed to save chat message from {} to {}.", senderId, receiverId);
            sendErrorMessageToClient(onlineSessions.get(senderId), "Failed to send message. Please try again.");
            return;
        }

        log.info("Message saved to DB: ID={}, From={}, To={}", chatMessage.getId(), senderId, receiverId);

        // 3. 判断接收方是否在线，并尝试直接发送
        if (userPresenceService.isUserOnline(receiverId)) {
            WebSocketSession receiverSession = onlineSessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                try {
                    // 将保存后的消息实体转换为 WebSocket 消息格式发送给接收方
                    // 这里 messageType 直接使用 ChatMessage 实体中的 Integer 类型
                    sendChatMessageToClient(receiverSession, WS_TYPE_CHAT, chatMessage.getSenderId(), chatMessage.getReceiverId(), chatMessage.getContent(), chatMessage.getMessageType(), chatMessage.getId(), chatMessage.getSendTime());
                    log.info("Message sent directly to online user {}: ID={}, Content='{}'", receiverId, chatMessage.getId(), content);
                } catch (IOException e) {
                    log.error("Failed to send message to online user {}: {}", receiverId, e.getMessage(), e);
                    sendErrorMessageToClient(onlineSessions.get(senderId), "Message sent but failed to deliver instantly to " + receiverId);
                }
            } else {
                log.warn("User {} reported online by Redis, but WebSocketSession not found or closed. Message ID {}.", receiverId, chatMessage.getId());
            }
        } else {
            log.info("Receiver {} is offline. Message ID {} stored as unread.", receiverId, chatMessage.getId());
            sendSystemMessageToClient(onlineSessions.get(senderId), "Your message has been sent to " + receiverId + ", they are currently offline.");
        }
    }

    /**
     * 处理客户端发送的已读确认消息
     *
     * @param clientMessage 客户端消息对象
     */
    private void handleReadAcknowledgement(WebSocketClientMessage clientMessage) {
        // 发送 read_ack 的是阅读者
        Long readerId = clientMessage.getSenderId();
        // 确认已读的是与哪个用户的消息（可选，如果确认所有来自某个用户发给我的消息）
        // Long targetUserId = clientMessage.getReceiverId();
        // 如果是确认单条消息已读
        Long messageId = clientMessage.getMessageId();

        if (readerId == null) {
            log.warn("Received read_ack with null readerId.");
            return;
        }

        try {
            // 标记当前用户收到的消息全为已读
            chatMessageService.markMessagesAsRead(readerId);
        } catch (Exception e) {
            log.error("Failed to process read acknowledgement for user {}: {}", readerId, e.getMessage(), e);
        }
    }


    // ---------------------- 辅助方法 ----------------------

    /**
     * 从 WebSocketSession 中获取 userId
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get(SaTokenWebSocketInterceptor.USER_ID_ATTR);
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            log.error("从 WebSocketSession attributes 中获取用户ID失败，类型不匹配或不存在: {}",
                    userIdObj != null ? userIdObj.getClass().getName() : "null");
            return null;
        }
    }

    /**
     * 发送系统、错误、警告消息给客户端
     *
     * @param session 目标 session
     * @param wsType  WebSocket 消息类型 (使用 WebSocketHandler 内部定义的常量)
     * @param content 消息内容
     */
    private void sendSystemMessageToClient(WebSocketSession session, Integer wsType, String content) throws IOException {
        if (session != null && session.isOpen()) {
            WebSocketServerMessage msg = new WebSocketServerMessage();
            msg.setType(wsType); // 这里直接使用整数 type
            msg.setContent(content);
            // 对于系统/错误/警告消息，senderId, receiverId, messageType, sendTime, messageId 均可为 null
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    private void sendSystemMessageToClient(WebSocketSession session, String content) throws IOException {
        sendSystemMessageToClient(session, WS_TYPE_SYSTEM_MESSAGE, content);
    }

    private void sendErrorMessageToClient(WebSocketSession session, String content) throws IOException {
        sendSystemMessageToClient(session, WS_TYPE_ERROR_MESSAGE, content);
    }

    private void sendWarningMessageToClient(WebSocketSession session, String content) throws IOException {
        sendSystemMessageToClient(session, WS_TYPE_WARNING_MESSAGE, content);
    }

    /**
     * 发送实际的聊天消息给客户端
     *
     * @param session     目标 session
     * @param wsType      WebSocket 消息类型 (例如 WS_TYPE_CHAT)
     * @param senderId    发送者ID
     * @param receiverId  接收者ID
     * @param content     消息内容
     * @param messageType 消息内容的类型 (例如 ChatMessageTypeEnum.TEXT.getCode())
     * @param messageId   消息在数据库中的ID
     * @param sendTime    消息发送时间
     */
    private void sendChatMessageToClient(WebSocketSession session, Integer wsType, Long senderId, Long receiverId, String content, Integer messageType, Long messageId, Date sendTime) throws IOException {
        if (session != null && session.isOpen()) {
            WebSocketServerMessage msg = new WebSocketServerMessage();
            msg.setType(wsType); // 例如 WS_TYPE_CHAT
            msg.setSenderId(senderId);
            msg.setReceiverId(receiverId);
            msg.setContent(content);
            msg.setMessageType(messageType); // 直接使用 Integer
            msg.setMessageId(messageId);
            msg.setSendTime(sendTime);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    // 用于解析客户端发来的 WebSocket 消息 (例如：从前端发来的 chat 消息)
    @Data
    static class WebSocketClientMessage {
        // WebSocket 消息类型，例如 WS_TYPE_CHAT, WS_TYPE_HEARTBEAT, WS_TYPE_READ_ACK
        private Integer type;
        // 客户端可能带上，但服务端会以 SaToken 为准
        private Long senderId;
        private Long receiverId;
        private String content;
        // 客户端可能传入的消息内容类型，例如 ChatMessageTypeEnum.TEXT.getCode()
        private Integer messageType;
        // 用于 read_ack 确认
        private Long messageId;
    }

    // 用于构建服务器发送给客户端的 WebSocket 消息
    @Data
    static class WebSocketServerMessage {
        // WebSocket 消息类型，例如 WS_TYPE_CHAT, WS_TYPE_SYSTEM_MESSAGE, WS_TYPE_ERROR_MESSAGE
        private Integer type;
        private Long senderId;
        // 消息的真正接收者 (客户端的 userId)
        private Long receiverId;
        private String content;
        // 消息内容的具体类型，例如 ChatMessageTypeEnum.TEXT.getCode()
        private Integer messageType;
        private Date sendTime;
        // 如果需要，可以添加消息ID，readStatus 等
        private Long messageId;
    }
}