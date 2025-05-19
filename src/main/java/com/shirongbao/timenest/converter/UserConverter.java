package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
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
