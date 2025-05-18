package com.shirongbao.timecapsule.converter;

import com.shirongbao.timecapsule.pojo.entity.Users;
import com.shirongbao.timecapsule.pojo.request.UsersDto;
import com.shirongbao.timecapsule.pojo.response.UsersVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: 用户转换器
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    UsersVo entityToResponseObject(Users users);

    Users requestObjectToEntity(UsersDto request);
}
