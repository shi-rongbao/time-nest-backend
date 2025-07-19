package com.shirongbao.timenest.pojo.bo;

import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 聊天消息bo类
 */
@Data
public class ChatMessagesBo {

    // 消息id
    private Long messageId;

    // 会话id
    private Long sessionId;

    // 发送者id
    private Long senderId;

    // 发送者昵称
    private String senderNickname;

    // 发送者头像
    private String senderAvatar;

    // 消息类型
    private Integer messageType;

    // 发送内容JSON格式的字符串或纯文本
    private String content;

    // 是否已撤回：1-已撤回；0-未撤回
    private Integer recalled;

    // 发送时间
    private Date createdAt;

}
