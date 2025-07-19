package com.shirongbao.timenest.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 聊天消息vo类
 */
@Data
public class ChatMessageVo {

    // 消息id
    private Long messageId;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

}
