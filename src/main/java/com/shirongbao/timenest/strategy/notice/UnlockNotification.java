package com.shirongbao.timenest.strategy.notice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.NotificationTypeEnum;
import com.shirongbao.timenest.common.enums.StatusEnum;
import com.shirongbao.timenest.converter.NotificationConverter;
import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.service.nest.TimeNestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 拾光纪解锁通知
 */
@Component
@RequiredArgsConstructor
public class UnlockNotification implements NotificationStrategy{

    private final TimeNestService timeNestService;

    @Override
    public int getCode() {
        return NotificationTypeEnum.UNLOCK_NOTICE.getCode();
    }

    @Override
    public Collection<? extends NotificationBo> combineNotification(List<Notification> notifications) {
        // 拿到所有的拾光纪id
        List<Long> timeNestIdList = notifications.stream().map(Notification::getNoticeId).collect(Collectors.toList());

        // 批量查询这些拾光纪
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TimeNest::getId, timeNestIdList);
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.eq(TimeNest::getNestStatus, StatusEnum.NORMAL.getCode());
        List<TimeNest> timeNestList = timeNestService.list(wrapper);
        // 转成map，key是nestId，value是nestTitle
        Map<Long, String> timeNestMap = timeNestList.stream().collect(Collectors.toMap(TimeNest::getId, TimeNest::getNestTitle));

        // 转换后组装nestTitle
        List<NotificationBo> notificationBoList = NotificationConverter.INSTANCE.notificationListToNotificationBoList(notifications);

        for (NotificationBo notificationBo : notificationBoList) {
            notificationBo.setTimeNestTitle(timeNestMap.get(notificationBo.getNoticeId()));
        }

        return notificationBoList;
    }

}
