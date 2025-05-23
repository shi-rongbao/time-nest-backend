package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友转换器
 */
@Mapper
public interface FriendshipsConverter {

    FriendshipsConverter INSTANCE = Mappers.getMapper(FriendshipsConverter.class);

}
