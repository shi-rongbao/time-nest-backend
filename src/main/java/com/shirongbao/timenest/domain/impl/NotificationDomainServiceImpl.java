package com.shirongbao.timenest.domain.impl;

import com.shirongbao.timenest.domain.NotificationDomainService;
import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;
import com.shirongbao.timenest.strategy.notice.NotificationStrategy;
import com.shirongbao.timenest.strategy.notice.NotificationStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知领域服务实现类(为了解决循环依赖)
 */
@Service("notificationDomainService")
@RequiredArgsConstructor
public class NotificationDomainServiceImpl implements NotificationDomainService {

    private final NotificationStrategyFactory notificationStrategyFactory;

    @Override
    public List<NotificationBo> combineNotification(List<Notification> notificationList) {
        // 根据通知类型进行分组
        Map<Integer, List<Notification>> groupedNotifications = notificationList.stream()
                .collect(Collectors.groupingBy(Notification::getNoticeType));

        List<NotificationBo> notificationBoList = new ArrayList<>();

        // 遍历每种类型，根据不同的类型执行不同的策略
        groupedNotifications.forEach((noticeType, notifications) -> {
            NotificationStrategy strategy = notificationStrategyFactory.getStrategy(noticeType);
            notificationBoList.addAll(strategy.combineNotification(notifications));
        });

        // 全都添加到返回结果中
        return notificationBoList;
    }

}
