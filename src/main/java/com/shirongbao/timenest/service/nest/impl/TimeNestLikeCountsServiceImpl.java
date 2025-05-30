package com.shirongbao.timenest.service.nest.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.TimeNestLikeCountMapper;
import com.shirongbao.timenest.pojo.entity.TimeNestLikeCounts;
import com.shirongbao.timenest.service.nest.TimeNestLikeCountsService;
import com.shirongbao.timenest.strategy.like.LikeStrategy;
import com.shirongbao.timenest.strategy.like.LikeStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 拾光纪点赞数服务实现类
 */
@Service("timeNestLikeCountService")
@RequiredArgsConstructor
public class TimeNestLikeCountsServiceImpl extends ServiceImpl<TimeNestLikeCountMapper, TimeNestLikeCounts> implements TimeNestLikeCountsService {

    private final ThreadPoolExecutor threadPool;

    private final LikeStrategyFactory likeStrategyFactory;

    @Override
    @Transactional
    public void likeTimeNest(Long nestId, Integer likeType) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        // 操作点赞
        LikeStrategy likeStrategy = likeStrategyFactory.getLikeStrategy(likeType);
        likeStrategy.operateLikes(nestId, currentUserId);

        // 异步的去更新点赞数
        threadPool.execute(() -> {
            // 根据拾光纪id查询点赞数记录
            LambdaQueryWrapper<TimeNestLikeCounts> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimeNestLikeCounts::getTimeNestId, nestId);
            TimeNestLikeCounts timeNestLikeCounts = getOne(wrapper);

            Long likeCount = likeStrategy.getLikeCount(timeNestLikeCounts);
            // 当前拾光纪不存在点赞记录时，返回-1，表示创建新的点赞记录
            if (likeCount == -1) {
                // 创建新的点赞记录
                timeNestLikeCounts = new TimeNestLikeCounts();
                timeNestLikeCounts.setTimeNestId(nestId);
                timeNestLikeCounts.setLikeCount(1L);
                save(timeNestLikeCounts);
                return;
            }

            // 更新点赞数
            timeNestLikeCounts.setLikeCount(likeCount);
            updateById(timeNestLikeCounts);
        });
    }

}
