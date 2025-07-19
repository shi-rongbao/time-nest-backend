package com.shirongbao.timenest.pojo.dto.mq;

import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 聊天消息mq dto
 */
@Data
public class ChatMessageMqDto {

    // 发送者id
    private Long senderId;

    // 会话id
    private Long sessionId;

    // 消息类型
    private Integer messageType;

    // 消息内容
    private String content;

    // 客户端消息id
    private String clientMessageId;

    // 使用服务器生成的时间
    private Date createdAt;

}
