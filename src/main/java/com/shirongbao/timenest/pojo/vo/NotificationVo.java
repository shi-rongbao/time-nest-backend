package com.shirongbao.timenest.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求通知vo类
 */
@Data
public class NotificationVo {

    // 主键id
    private Long id;

    // 要通知的用户id
    private Long noticeUserId;

    // 发送通知的id（type=1：好友用户id：type=2：拾光纪id）
    private Long noticeId;

    // 通知类型：1-好友请求通知；2-拾光纪解锁通知
    private Integer noticeType;

    // 请求用户账号
    private String requestUserAccount;

    // 拾光纪的标题
    private String timeNestTitle;

    // 好友申请表id
    private Long friendRequestsId;

    // 是否已读：1-已读；0-未读
    private Integer isRead;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

}
