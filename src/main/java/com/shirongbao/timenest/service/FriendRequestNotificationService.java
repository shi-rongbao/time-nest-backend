package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.bo.FriendRequestNotificationBo;
import com.shirongbao.timenest.pojo.entity.FriendRequestNotification;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求通知服务接口
 */
public interface FriendRequestNotificationService extends IService<FriendRequestNotification> {

    // 保存好友申请记录通知
    void saveNotification(Long friendRequestId, Long noticeUserId, Long senderUserId);

    // 获取未读消息
    List<FriendRequestNotification> getUnreadNotifications(Long noticeUserId);

    // 标记为已读
    void markAsRead(Long noticeId);
}