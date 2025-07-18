package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

    UsersBo usersDtoToUsersBo(UsersDto usersDto);

    List<UsersBo> usersListToUsersBoList(List<Users> usersList);

    Users usersVoToEntity(UsersVo userInfo);
}
