package com.shirongbao.timenest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.dao.UserLikesMapper;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.entity.UserLikes;
import com.shirongbao.timenest.service.UserLikesService;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 用户点赞服务实现类
 */
@Service("userLikeService")
public class UserLikesServiceImpl extends ServiceImpl<UserLikesMapper, UserLikes> implements UserLikesService {

    @Override
    public UserLikes queryUserLike(long currentUserId, Long nestId) {
        LambdaQueryWrapper<UserLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLikes::getUserId, currentUserId);
        wrapper.eq(UserLikes::getTimeNestId, nestId);
        return getOne(wrapper);
    }

    @Override
    public Page<UserLikes> queryMyLikeTimeNestList(TimeNestDto timeNestDto, Long userId) {
        Page<UserLikes> page = new Page<>(timeNestDto.getPageNum(), timeNestDto.getPageSize());
        LambdaQueryWrapper<UserLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLikes::getUserId, userId);
        wrapper.eq(UserLikes::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        return page(page, wrapper);
    }

}
