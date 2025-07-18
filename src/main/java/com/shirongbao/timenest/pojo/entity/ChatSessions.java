package com.shirongbao.timenest.pojo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-15
 * @description: 聊天会话表
 */
@Data
public class ChatSessions {
    // 主键ID
    private Long id;

    // 会话类型：1-单聊；2-群聊
    private Integer sessionType;

    // 群聊名称（单聊时可为null）
    private String groupName;

    // 群头像（单聊时可为null）
    private String groupAvatar;

    // 最后一条消息的文本摘要
    private String lastMessageContent;

    // 最后一条消息的发送时间
    private Date lastMessageTime;

    // 创建者ID（群聊）
    private Long creatorId;

    // 逻辑删除标识：1-已删除；0-未删除
    private Integer isDeleted;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;
}
