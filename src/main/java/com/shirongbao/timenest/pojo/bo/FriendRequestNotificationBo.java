package com.shirongbao.timenest.pojo.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求提醒bo类
 */
@Data
public class FriendRequestNotificationBo {

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 要通知的用户id
    private Long noticeUserId;

    // 发送请求的用户id
    private Long senderUserId;

    // 发送请求的用户账号
    private String requestUserAccount;

    // 好友申请表id
    private Long friendRequestsId;

    // 是否已读：1-已读；0-未读
    private Integer isRead;

    // 创建时间
    private Date createdAt;

}
