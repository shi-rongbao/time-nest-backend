package com.shirongbao.timenest.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-17
 * @description: 聊天会话VO类
 */
@Data
public class ChatSessionVo {

    // 会话id
    private Long sessionId;

    // 会话类型
    private Integer sessionType;

    // 动态字段：群聊时是群名，单聊时是对方昵称
    private String displayName;

    // 动态字段：群聊时是群头像，单聊时是对方头像
    private String displayAvatar;

    // 最后信息内容
    private String lastMessageContent;

    // 最后发送时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastMessageTime;

    // 未读消息数
    private Integer unreadCount;

}