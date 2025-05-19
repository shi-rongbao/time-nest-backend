package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求实体类
 */
@Data
public class FriendRequests {

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 发送请求的用户id
    private Long senderUserId;

    // 接收请求的用户id
    private Long receiverUserId;

    // 请求信息
    private String requestMessage;

    // 处理状态：1-已处理；0-未处理
    private Integer processingStatus;

    // 逻辑删除：1-已删除；0-未删除
    private Integer isDeleted;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;

}
