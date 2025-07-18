package com.shirongbao.timenest.pojo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-15
 * @description: 聊天消息表
 */
@Data
public class ChatMessages {

    // 消息的唯一id（雪花算法生成id）
    private Long id;

    // 该消息所属的会话id
    private Long sessionId;

    // 发送者id
    private Long senderId;

    // 消息类型：1-TEXT；2-IMAGE；3-SYSTEM
    private int messageType;

    // 消息内容。TEXT存文本，JSON可以存更复杂的结构（如图片URL、文件名、文件大小等）
    private String content;

    // 消息发送时间
    private Date sendAt;

    // 是否已撤回：1-已撤回；0-未撤回
    private int recalled;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;
}
