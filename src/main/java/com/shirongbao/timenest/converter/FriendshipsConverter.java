package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.pojo.bo.FriendRequestNotificationBo;
import com.shirongbao.timenest.pojo.entity.FriendRequestNotification;
import com.shirongbao.timenest.pojo.vo.FriendRequestNotificationVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description:
 */
@Mapper
public interface FriendshipsConverter {

    FriendshipsConverter INSTANCE = Mappers.getMapper(FriendshipsConverter.class);

    List<FriendRequestNotificationVo> friendRequestNotificationListToFriendRequestNotificationVoList(List<FriendRequestNotification> friendRequestNotificationList);

    List<FriendRequestNotificationVo> friendRequestNotificationBoListToFriendRequestNotificationVoList(List<FriendRequestNotificationBo> friendRequestNotificationBoList);

    List<FriendRequestNotificationBo> friendRequestNotificationListToFriendRequestNotificationBoList(List<FriendRequestNotification> friendRequestNotificationList);
}
