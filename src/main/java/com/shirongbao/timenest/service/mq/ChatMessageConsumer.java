package com.shirongbao.timenest.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.timenest.common.constant.MQConstant;
import com.shirongbao.timenest.pojo.dto.mq.ChatMessageMqDto;
import com.shirongbao.timenest.pojo.entity.ChatMessages;
import com.shirongbao.timenest.service.chat.ChatService;
import com.shirongbao.timenest.websocket.MainWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 聊天消息消费者
 */
@Service("chatMessageConsumer")
@Slf4j
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final ObjectMapper objectMapper;

    private final ChatService chatService;

    private final MainWebSocketHandler mainWebSocketHandler;

    @RabbitListener(queues = MQConstant.CHAT_MESSAGE_QUEUE)
    @Transactional
    public void handleMessagePersistence(String messagePayload) {
        log.info("从MQ收到待持久化的消息: {}", messagePayload);
        try {
            // 1. 将JSON字符串反序列化为DTO对象
            ChatMessageMqDto mqDto = objectMapper.readValue(messagePayload, ChatMessageMqDto.class);

            //    a. 将消息保存到 chat_messages 表
            ChatMessages chatMessages = chatService.saveChatMessages(mqDto);
            //    b. 更新 chat_sessions 表的最后消息摘要
            chatService.updateChatSessionAbstract(chatMessages);
            //    c. 为离线用户增加 chat_session_members 表的未读数
            chatService.increUnreadCount(mqDto.getSessionId(), mqDto.getSenderId());

            // 3. 注册一个“事务成功提交后”的回调，用于发送消息回执
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 构建回执消息体
                    Map<String, Object> ackData = Map.of(
                            "clientMessageId", mqDto.getClientMessageId(),
                            "messageId", chatMessages.getId(),
                            "createdAt", chatMessages.getSendAt()
                    );
                    Map<String, Object> ackMessage = Map.of(
                            "type", "message_ack",
                            "data", ackData
                    );

                    try {
                        String ackJson = objectMapper.writeValueAsString(ackMessage);
                        // 向原始发送方推送“已送达”回执
                        mainWebSocketHandler.sendMessageToUser(mqDto.getSenderId(), ackJson);
                    } catch (Exception e) {
                        log.error("序列化或发送ACK消息失败", e);
                    }
                }
            });

        } catch (Exception e) {
            log.error("处理MQ消息失败！消息内容: {}", messagePayload, e);
        }
    }

}
