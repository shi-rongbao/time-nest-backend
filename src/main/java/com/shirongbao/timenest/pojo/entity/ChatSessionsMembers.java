package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-17
 * @description: 聊天会话成员表
 */
@Data
public class ChatSessionsMembers {
    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 关联的会话ID
    private Long sessionId;

    // 关联的用户ID
    private Long userId;

    // 该用户在此会话中的未读消息数
    private Integer unreadCount;

    // 用户最后确认收到消息的时间（用于已读回执）
    private Date lastAckTime;

    // 在群聊中的角色（单聊时都是MEMBER），枚举值：1-MEMBER，2-ADMIN，3-OWNER
    private Integer role;

    // 加入会话的时间
    private Date joinedAt;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;
}