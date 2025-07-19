package com.shirongbao.timenest.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.timenest.common.constant.MQConstant;
import com.shirongbao.timenest.pojo.dto.mq.ChatMessageMqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: mq生产者服务
 */
@Slf4j
@RequiredArgsConstructor
@Service("mqProducerService")
public class MQProducerService {

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;


    /**
     * 发送需要持久化的聊天消息
     * @param mqDto 消息数据
     */
    public void sendChatMessageToPersist(ChatMessageMqDto mqDto) {
        try {
            // 将对象序列化为JSON字符串进行传输
            String message = objectMapper.writeValueAsString(mqDto);
            rabbitTemplate.convertAndSend(MQConstant.CHAT_EXCHANGE, MQConstant.PERSIST_ROUTING_KEY, message);
            log.info("成功发送聊天消息到MQ, SessionId: {}", mqDto.getSessionId());
        } catch (Exception e) {
            log.error("发送聊天消息到MQ失败, SessionId: {}", mqDto.getSessionId(), e);
        }
    }
}
