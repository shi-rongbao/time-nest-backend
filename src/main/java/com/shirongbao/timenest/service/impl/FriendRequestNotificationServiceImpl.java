package com.shirongbao.timenest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.enums.FriendRequestNotificationIsReadEnum;
import com.shirongbao.timenest.dao.FriendRequestNotificationMapper;
import com.shirongbao.timenest.pojo.entity.FriendRequestNotification;
import com.shirongbao.timenest.service.FriendRequestNotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求通知服务实现类
 */
@Service("friendRequestNotificationService")
public class FriendRequestNotificationServiceImpl extends ServiceImpl<FriendRequestNotificationMapper, FriendRequestNotification> implements FriendRequestNotificationService {

    @Override
    public void saveNotification(Long friendRequestId, Long receiverUserId) {
        FriendRequestNotification friendRequestNotification = new FriendRequestNotification();
        friendRequestNotification.setFriendRequestsId(friendRequestId);
        friendRequestNotification.setNoticeUserId(receiverUserId);
        save(friendRequestNotification);
    }

    @Override
    public List<FriendRequestNotification> getUnreadNotifications(Long noticeUserId) {
        LambdaQueryWrapper<FriendRequestNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequestNotification::getIsRead, FriendRequestNotificationIsReadEnum.NOT_READ.getCode());
        wrapper.eq(FriendRequestNotification::getNoticeUserId, noticeUserId);
        wrapper.orderByDesc(FriendRequestNotification::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public void markAsRead(Long noticeId) {
        FriendRequestNotification friendRequestNotification = new FriendRequestNotification();
        friendRequestNotification.setId(noticeId);
        friendRequestNotification.setIsRead(FriendRequestNotificationIsReadEnum.READ.getCode());
        updateById(friendRequestNotification);
    }

}
