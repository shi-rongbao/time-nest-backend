package com.shirongbao.timenest.common.constant;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: mq常量
 */
public final class MQConstant {

    private MQConstant () {}

    /** 聊天交换机 */
    public static final String CHAT_EXCHANGE = "chat.exchange";

    /** 消息路由键 */
    public static final String PERSIST_ROUTING_KEY = "message.persist";

    /** 聊天消息队列 */
    public static final String CHAT_MESSAGE_QUEUE = "chat.message.persist.queue";

}
