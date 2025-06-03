package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 聊天消息实体类
 */
@Data
@Accessors(chain = true)
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID (主键，自动生成)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 接收者用户ID (如果是单聊)
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-text；2-image；3-file
     */
    private Integer messageType;

    /**
     * 消息读状态：1-已读；0-未读
     */
    private Integer readStatus;

    /**
     * 消息发送时间
     */
    private Date sendTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}