package com.shirongbao.timenest.domain;

import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知领域服务(为了解决循环依赖)
 */
public interface NotificationDomainService {

    // 组装notificationBoList
    List<NotificationBo> combineNotification(List<Notification> notificationList);

}
