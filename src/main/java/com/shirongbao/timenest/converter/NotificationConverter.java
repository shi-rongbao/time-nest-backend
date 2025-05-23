package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;
import com.shirongbao.timenest.pojo.vo.NotificationVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知转换器
 */
@Mapper
public interface NotificationConverter {

    NotificationConverter INSTANCE = Mappers.getMapper(NotificationConverter.class);

    List<NotificationVo> notificationBoListToNotificationVoList(List<NotificationBo> notificationBoList);

    List<NotificationBo> notificationListToNotificationBoList(List<Notification> notificationList);
}
