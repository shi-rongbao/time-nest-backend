package com.shirongbao.timenest.pojo.bo;

import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description:
 */
@Data
public class ChatSessionBo {

    // 会话ID
    private Long sessionId;

    // 会话类型：1-单聊；2-群聊
    private Integer sessionType;

    // 动态字段：群聊时是群名，单聊时是对方昵称
    private String displayName;

    // 动态字段：群聊时是群头像，单聊时是对方头像
    private String displayAvatar;

    // 对方用户ID（单聊时使用）
    private Long targetUserId;

    // 对方用户昵称（单聊时使用）
    private String targetNickName;

    // 对方用户头像（单聊时使用）
    private String targetAvatarUrl;

    // 群聊名称（群聊时使用）
    private String groupName;

    // 群聊头像（群聊时使用）
    private String groupAvatar;

    // 未读消息数
    private Integer unreadCount;

    // 最后一条消息内容
    private String lastMessageContent;

    // 最后一条消息类型
    private Integer lastMessageType;

    // 最后一条消息发送者ID
    private Long lastSenderId;

    // 最后一条消息发送者昵称
    private String lastSenderName;

    // 最后一条消息发送时间
    private Date lastMessageTime;

}
