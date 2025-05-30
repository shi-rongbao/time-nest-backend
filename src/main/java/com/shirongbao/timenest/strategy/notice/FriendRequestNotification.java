package com.shirongbao.timenest.strategy.notice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.NotificationTypeEnum;
import com.shirongbao.timenest.common.enums.StatusEnum;
import com.shirongbao.timenest.converter.NotificationConverter;
import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 好友申请通知
 */
@Component
@RequiredArgsConstructor
public class FriendRequestNotification implements NotificationStrategy{

    private final UserService userService;

    @Override
    public int getCode() {
        return NotificationTypeEnum.FRIEND_REQUEST_NOTICE.getCode();
    }

    @Override
    public Collection<? extends NotificationBo> combineNotification(List<Notification> notifications) {
        // 拿到未读通知中所有的发请求的用户id
        List<Long> sendUserIdList = notifications.stream().map(Notification::getNoticeId).collect(Collectors.toList());
        // 批量查询到全部用户
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Users::getId, sendUserIdList);
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.eq(Users::getStatus, StatusEnum.NORMAL.getCode());
        List<Users> usersList = userService.list(wrapper);
        // 转成map，key是usersId，value是userAccount
        Map<Long, String> userAccountMap = usersList.stream().collect(Collectors.toMap(Users::getId, Users::getUserAccount));

        // 转换后组装userAccount
        List<NotificationBo> notificationBoList = NotificationConverter.INSTANCE.notificationListToNotificationBoList(notifications);

        for (NotificationBo notificationBo : notificationBoList) {
            notificationBo.setRequestUserAccount(userAccountMap.get(notificationBo.getNoticeId()));
        }

        return notificationBoList;
    }

}
