package com.shirongbao.timenest.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.dao.FriendshipsMapper;
import com.shirongbao.timenest.pojo.entity.Friendships;
import com.shirongbao.timenest.service.FriendshipsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
  * @author: ShiRongbao
  * @date: 2025-05-19
  * @description: 好友关系服务实现类
  */
@Service("friendshipsService")
@RequiredArgsConstructor
public class FriendshipsServiceImpl extends ServiceImpl<FriendshipsMapper, Friendships> implements FriendshipsService {

    private final ThreadPoolExecutor labelThreadPool;

    @Override
    public List<Friendships> getFriendList(long currentUserId) throws ExecutionException, InterruptedException {
        // 异步的去查好友表，拿到当前用户id的记录
        CompletableFuture<List<Friendships>> user1FriendshipsList = CompletableFuture.supplyAsync(() -> {
            LambdaQueryWrapper<Friendships> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Friendships::getUserId1, currentUserId);
            wrapper.eq(Friendships::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
            return this.list(wrapper);
        }, labelThreadPool);

        CompletableFuture<List<Friendships>> user2FriendshipsList = CompletableFuture.supplyAsync(() -> {
            LambdaQueryWrapper<Friendships> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Friendships::getUserId2, currentUserId);
            wrapper.eq(Friendships::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
            return this.list(wrapper);
        }, labelThreadPool);

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(user1FriendshipsList, user2FriendshipsList);
        allFutures.join();

        // 全部添加起来
        List<Friendships> friendshipsList1 = user1FriendshipsList.get();
        List<Friendships> friendshipsList2 = user2FriendshipsList.get();
        friendshipsList1.addAll(friendshipsList2);

        return friendshipsList1;
    }

    @Override
    public boolean checkIsFriend(Long id) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        QueryWrapper<Friendships> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
                .and(w -> w
                        .and(w1 -> w1.eq("user_id1", id).eq("user_id2", currentUserId))
                        .or(w2 -> w2.eq("user_id1", currentUserId).eq("user_id2", id))
                );

        Friendships relation = getOne(wrapper);
        return relation != null;
    }
}
