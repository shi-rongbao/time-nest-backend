package com.shirongbao.timenest.config;

import com.shirongbao.timenest.common.constant.MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: rabbitmq配置类
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(MQConstant.CHAT_EXCHANGE, true, false);
    }

    @Bean
    public Queue chatMessagePersistQueue() {
        return new Queue(MQConstant.CHAT_MESSAGE_QUEUE, true);
    }

    @Bean
    public Binding chatMessagePersistBinding() {
        return BindingBuilder
                .bind(chatMessagePersistQueue())
                .to(chatExchange())
                .with(MQConstant.PERSIST_ROUTING_KEY);
    }
}
