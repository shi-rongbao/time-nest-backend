package com.shirongbao.timenest.service.nest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.entity.UserLikes;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 用户点赞服务接口
 */
public interface UserLikesService extends IService<UserLikes> {

    // 根据用户id和拾光纪id查询用户点赞记录
    UserLikes queryUserLike(long currentUserId, Long nestId);

    // 分页查看“我”点赞的记录
    Page<UserLikes> queryMyLikeTimeNestList(TimeNestDto timeNestDto, Long userId);
}
