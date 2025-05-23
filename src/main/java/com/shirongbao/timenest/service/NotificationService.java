package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.Notification;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    // 保存好友申请记录通知
    void saveNotification(Long friendRequestId, Long noticeUserId, Long senderUserId);

    // 获取未读消息
    List<Notification> getUnreadNotifications(Long noticeUserId);

    // 标记为已读
    void markAsRead(Long noticeId);

    // 记录解锁通知
    void recordUnlockNotice(List<Long> userIdList, Long noticeId);

}