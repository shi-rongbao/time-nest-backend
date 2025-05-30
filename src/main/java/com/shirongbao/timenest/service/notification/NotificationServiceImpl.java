package com.shirongbao.timenest.service.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.enums.NotificationIsReadEnum;
import com.shirongbao.timenest.common.enums.NotificationTypeEnum;
import com.shirongbao.timenest.dao.FriendRequestNotificationMapper;
import com.shirongbao.timenest.pojo.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求通知服务实现类
 */
@Service("notificationService")
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<FriendRequestNotificationMapper, Notification> implements NotificationService {

    @Override
    public void saveNotification(Long friendRequestId, Long receiverUserId, Long senderUserId) {
        Notification notification = new Notification();
        notification.setFriendRequestsId(friendRequestId);
        notification.setNoticeUserId(receiverUserId);
        notification.setNoticeId(senderUserId);
        notification.setNoticeType(NotificationTypeEnum.FRIEND_REQUEST_NOTICE.getCode());
        save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long noticeUserId) {
        // 先查到未读的通知
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, NotificationIsReadEnum.NOT_READ.getCode());
        wrapper.eq(Notification::getNoticeUserId, noticeUserId);
        wrapper.orderByDesc(Notification::getCreatedAt);
        wrapper.last("limit 6");
        return list(wrapper);
    }

    @Override
    public void markAsRead(Long noticeId) {
        Notification notification = new Notification();
        notification.setId(noticeId);
        notification.setIsRead(NotificationIsReadEnum.READ.getCode());
        updateById(notification);
    }

    @Override
    @Transactional
    public void recordUnlockNotice(List<Long> userIdList, Long noticeId) {
        List<Notification> notificationList = new ArrayList<>();
        for (Long userId : userIdList) {
            Notification notification = new Notification();
            notification.setNoticeUserId(userId);
            notification.setNoticeId(noticeId);
            notification.setNoticeType(NotificationTypeEnum.UNLOCK_NOTICE.getCode());
            notification.setIsRead(NotificationIsReadEnum.NOT_READ.getCode());
            notificationList.add(notification);
        }

        // 批量保存
        saveBatch(notificationList);
    }

}
