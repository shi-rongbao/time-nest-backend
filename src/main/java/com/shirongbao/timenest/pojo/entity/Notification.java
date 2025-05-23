package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 通知信息表
 */
@Data
public class Notification {

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 要通知的用户id
    private Long noticeUserId;

    // 发送通知的id（type=1：好友用户id：type=2：拾光纪id）
    private Long noticeId;

    // 通知类型：1-好友请求通知；2-拾光纪解锁通知
    private Integer noticeType;

    // 好友申请表id
    private Long friendRequestsId;

    // 是否已读：1-已读；0-未读
    private Integer isRead;

    // 创建时间
    private Date createdAt;

}
