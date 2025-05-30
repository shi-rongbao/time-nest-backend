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
 * @description: 取消点赞策略
 */
@Component
@RequiredArgsConstructor
public class UnLikes implements LikeStrategy{

    private final UserLikesService userLikesService;

    @Override
    public Integer getCode() {
        return LikeTypeEnum.NOT_LIKE.getCode();
    }

    @Override
    public void operateLikes(Long nestId, long currentUserId) {
        // 查到用户是否点赞过这条记录
        UserLikes userLikes = userLikesService.queryUserLike(currentUserId, nestId);
        if (Objects.isNull(userLikes)) {
            throw new RuntimeException("当前数据异常，请刷新页面后重试");
        }

        // 没有点赞却要取消
        if (userLikes.getIsDeleted().equals(IsDeletedEnum.DELETED.getCode())) {
            throw new RuntimeException("当前还未点赞，不能取消，请刷新页面");
        }

        // 取消点赞
        userLikes.setIsDeleted(IsDeletedEnum.DELETED.getCode());
        userLikesService.updateById(userLikes);
    }

    // 获取点赞数
    @Override
    public Long getLikeCount(TimeNestLikeCounts timeNestLikeCounts) {
        if (Objects.isNull(timeNestLikeCounts)) {
            throw new RuntimeException("当前数据异常，请刷新后再试");
        }

        // 返回点赞数
        return timeNestLikeCounts.getLikeCount() - 1;
    }

}
