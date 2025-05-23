package com.shirongbao.timenest.strategy.notice;

import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;

import java.util.Collection;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知策略接口
 */
public interface NotificationStrategy {

    // 获取nest类型
    int getCode();

    Collection<? extends NotificationBo> combineNotification(List<Notification> notifications);
}
