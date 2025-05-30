package com.shirongbao.timenest.strategy.like;

import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.LikeTypeEnum;
import com.shirongbao.timenest.pojo.entity.TimeNestLikeCounts;
import com.shirongbao.timenest.pojo.entity.UserLikes;
import com.shirongbao.timenest.service.nest.UserLikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 点赞策略
 */
@Component
@RequiredArgsConstructor
public class Likes implements LikeStrategy{

    private final UserLikesService userLikesService;

    @Override
    public Integer getCode() {
        return LikeTypeEnum.LIKE.getCode();
    }

    @Override
    public void operateLikes(Long nestId, long currentUserId) {
        // 查到用户是否点赞过这条记录
        UserLikes userLikes = userLikesService.queryUserLike(currentUserId, nestId);
        if (Objects.isNull(userLikes)) {
            // 新增一条记录
            UserLikes newUserLikes = new UserLikes();
            newUserLikes.setTimeNestId(nestId);
            newUserLikes.setUserId(currentUserId);
            newUserLikes.setIsDeleted(IsDeletedEnum.NOT_DELETED.getCode());
            userLikesService.save(newUserLikes);
            return;
        }

        // 用户点赞过
        if (userLikes.getIsDeleted().equals(IsDeletedEnum.NOT_DELETED.getCode())) {
            throw new RuntimeException("你已经给当前拾光纪点过赞了，请刷新页面！");
        }

        // 更新点赞状态
        userLikes.setIsDeleted(IsDeletedEnum.NOT_DELETED.getCode());
        userLikesService.updateById(userLikes);
    }

    @Override
    public Long getLikeCount(TimeNestLikeCounts timeNestLikeCounts) {
        if (Objects.isNull(timeNestLikeCounts)) {
            return -1L;
        }

        // 返回点赞数+1
        return timeNestLikeCounts.getLikeCount() + 1;
    }
}
