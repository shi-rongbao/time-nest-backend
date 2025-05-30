package com.shirongbao.timenest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.timenest.service.chat.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final UserPresenceService userPresenceService;

    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取 userId。更安全的做法是通过 Spring Security 或 JWT 获取认证信息
        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            log.warn("WebSocket connection refused: userId not found in session for {}. Closing session.", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Missing user ID"));
            return;
        }

        // 验证用户合法性（例如用户是否存在于数据库）
        if (false) {
            log.warn("WebSocket connection refused: Invalid user ID {}. Closing session.", userId);
            session.close(CloseStatus.SERVER_ERROR.withReason("Invalid user"));
            return;
        }

        // 检查是否已经存在该用户的 WebSocketSession，处理重复连接
        if (onlineSessions.containsKey(userId)) {
            log.warn("User {} already has an active WebSocket session. Closing previous session and establishing new one.", userId);
            WebSocketSession oldSession = onlineSessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                oldSession.close(CloseStatus.POLICY_VIOLATION.withReason("New connection established for the same user"));
            }
        }

        // 将用户ID和WebSocketSession关联
        onlineSessions.put(userId, session);
        // 调用 ChatService 注册用户上线状态到 Redis
        userPresenceService.userOnline(userId);

        log.info("User {} connected. Session ID: {}", userId, session.getId());

        // 通知客户端连接成功
        sendSystemMessage(session, "Welcome " + userId + "! Connected to chat server.");

        // TODO: 通知该用户的朋友其已上线 (可以通过消息队列或单独的通知服务)
    }

    /**
     * 接收到文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) {
            sendErrorMessage(session, "Unauthorized user. Please reconnect with a valid user ID.");
            return;
        }

        // 收到消息也算一次心跳，更新心跳时间
        try {
            userPresenceService.updateHeartbeat(senderId);
        } catch (Exception e) {
            log.error("Failed to update heartbeat for user {}: {}", senderId, e.getMessage());
            // 此时可以继续处理消息，但需要注意心跳异常可能导致用户被标记为离线
        }


        log.debug("Received message from {}: {}", senderId, message.getPayload());

        ChatMessage chatMessage = null;
        try {
            chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            chatMessage.setSenderId(senderId); // 确保发送方ID是服务端确定的，防止客户端伪造
        } catch (IOException e) {
            log.error("Failed to parse message from {}: {}, error: {}", senderId, message.getPayload(), e.getMessage());
            sendErrorMessage(session, "Invalid message format.");
            return;
        }

        // 处理不同类型的消息
        switch (chatMessage.getType()) {
            case "heartbeat":
                // 心跳消息，ChatService.updateHeartbeat(senderId) 已经处理，无需额外回复
                // 如果需要心跳确认，可以回复：
                // sendSystemMessage(session, "heartbeat_ack");
                break;
            case "chat":
                handleChatMessage(senderId, chatMessage);
                break;
            case "system_command": // 示例：可以处理一些系统命令
                handleSystemCommand(senderId, chatMessage);
                break;
            default:
                sendErrorMessage(session, "Unknown message type: " + chatMessage.getType());
        }
    }

    /**
     * 连接关闭后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            // 移除内存中的会话
            onlineSessions.remove(userId);
            // 调用 ChatService 标记用户离线
            userPresenceService.userOffline(userId);
            log.info("User {} disconnected. Session ID: {}, Status: {}", userId, session.getId(), status);
            // TODO: 通知该用户的朋友其已下线
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
        // 可以在这里记录错误，尝试关闭会话等
    }

    // ---------------------- 辅助方法 ----------------------

    /**
     * 处理聊天消息的转发逻辑
     */
    private void handleChatMessage(Long senderId, ChatMessage chatMessage) throws IOException {
        Long receiverId = chatMessage.getReceiverId();
        String content = chatMessage.getContent();
        WebSocketSession senderSession = onlineSessions.get(senderId);

        if (receiverId == null || content == null || content.isEmpty()) {
            sendErrorMessage(senderSession, "Missing receiverId or content for chat message.");
            return;
        }

        // 检查好友关系
        if (!userPresenceService.isFriend(senderId, receiverId)) {
            sendErrorMessage(senderSession, "You are not friends with " + receiverId + ".");
            return;
        }

        // 检查接收方是否在线
        if (userPresenceService.isUserOnline(receiverId)) {
            WebSocketSession receiverSession = onlineSessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                // 构建转发给接收方的消息
                String forwardedMessage = objectMapper.writeValueAsString(new ChatMessage("chat", senderId, receiverId, content));
                receiverSession.sendMessage(new TextMessage(forwardedMessage));
                log.info("Message from {} forwarded to {}", senderId, receiverId);
                // 给发送方一个确认
                sendSystemMessage(senderSession, "Message sent to " + receiverId + ".");
            } else {
                // 这种情况理论上不应该发生，因为 chatService.isUserOnline 返回 true
                // 但如果 Redis 状态更新有延迟或 Redis 连接不稳定，可能出现
                log.warn("User {} is online in Redis but no active WebSocket session found or session is closed. Message from {} will be handled as offline.", receiverId, senderId);
                handleOfflineMessage(senderId, receiverId, content, senderSession);
            }
        } else {
            // 接收方不在线，处理离线消息
            log.info("User {} is offline. Message from {} will be stored.", receiverId, senderId);
            handleOfflineMessage(senderId, receiverId, content, senderSession);
        }
    }

    /**
     * 处理离线消息的存储和通知（简化版，实际需要持久化）
     */
    private void handleOfflineMessage(Long senderId, Long receiverId, String content, WebSocketSession senderSession) throws IOException {
        // TODO: 实际应用中，这里需要将消息存储到数据库或消息队列，等待用户上线后推送
        sendWarningMessage(senderSession, receiverId + " is offline. Message will be delivered later.");
        // chatService.storeOfflineMessage(senderId, receiverId, content);
    }

    /**
     * 示例：处理系统命令
     */
    private void handleSystemCommand(Long senderId, ChatMessage chatMessage) throws IOException {
        WebSocketSession senderSession = onlineSessions.get(senderId);
        // 根据 chatMessage.content 进行不同的命令处理
        if ("get_online_users".equals(chatMessage.getContent())) {
            Set<String> onlineUserIds = userPresenceService.getAllOnlineUsers();
            sendSystemMessage(senderSession, "Online users: " + onlineUserIds.toString());
        } else {
            sendErrorMessage(senderSession, "Unknown system command: " + chatMessage.getContent());
        }
    }


    /**
     * 从 WebSocketSession 中获取 userId
     * 这是关键的安全点，需要根据您的认证体系进行修改
     * 推荐从认证上下文或HTTP Session中获取，而不是URL参数
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        // 方式1: 从 URI 参数中获取 (例如 ws://localhost:8080/chat?userId=1001)
        // 方便测试，但不安全。生产环境强烈不建议直接从URL获取敏感信息。
        Map<String, String> params = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().toSingleValueMap();
        String userIdStr = params.get("userId");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.valueOf(userIdStr);
            } catch (NumberFormatException e) {
                log.error("Invalid userId format in URI: {}", userIdStr);
                return null;
            }
        }

        // 方式2: 从 WebSocketSession attributes 中获取 (如果使用了 HttpSessionHandshakeInterceptor)
        // 在 HTTP 握手阶段，可以将已登录用户的 ID 存入 attributes
        // Long loggedInUserId = (Long) session.getAttributes().get("loggedInUserId");
        // if (loggedInUserId != null) {
        //     return loggedInUserId;
        // }

        // 方式3: 如果有 JWT 认证，可以在握手拦截器中解析 JWT 并设置到 attributes
        // String jwtToken = ...; // 从请求头或cookie获取
        // Long userIdFromJwt = jwtService.getUserIdFromToken(jwtToken);
        // if (userIdFromJwt != null) {
        //     return userIdFromJwt;
        // }

        return null;
    }

    // ---------------------- 消息发送辅助方法 ----------------------

    private void sendMessage(WebSocketSession session, String type, String content, String senderId, String receiverId) throws IOException {
        if (session != null && session.isOpen()) {
            ChatMessage msg = new ChatMessage(type, Long.valueOf(senderId), Long.valueOf(receiverId), content);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    private void sendSystemMessage(WebSocketSession session, String content) throws IOException {
        sendMessage(session, "system", content, null, null);
    }

    private void sendErrorMessage(WebSocketSession session, String content) throws IOException {
        sendMessage(session, "error", content, null, null);
    }

    private void sendWarningMessage(WebSocketSession session, String content) throws IOException {
        sendMessage(session, "warning", content, null, null);
    }
}

// 聊天消息的数据结构 (JSON格式)
// 注意：receiverId 在这里使用 Long 类型，与 ChatService 保持一致
class ChatMessage {
    private String type; // "chat", "heartbeat", "system", "error", "ack", "warning", "system_command"
    private Long senderId; // 修改为 Long
    private Long receiverId; // 修改为 Long
    private String content;

    // Getters and Setters (Jackson需要)
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ChatMessage() {}
    public ChatMessage(String type, Long senderId, Long receiverId, String content) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }
}