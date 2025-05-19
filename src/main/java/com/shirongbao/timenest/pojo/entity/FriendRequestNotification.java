package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友申请通知表
 */
@Data
public class FriendRequestNotification {

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 要通知的用户id
    private Long noticeUserId;

    // 好友申请表id
    private Long friendRequestsId;

    // 是否已读：1-已读；0-未读
    private Integer isRead;

    // 创建时间
    private Date createdAt;

}
