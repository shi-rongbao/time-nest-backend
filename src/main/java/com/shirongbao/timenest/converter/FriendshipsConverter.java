package com.shirongbao.timenest.converter;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友转换器
 */
@Mapper
public interface FriendshipsConverter {

    FriendshipsConverter INSTANCE = Mappers.getMapper(FriendshipsConverter.class);

}
