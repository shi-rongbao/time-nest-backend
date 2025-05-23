package com.shirongbao.timenest.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.ProcessingResultEnum;
import com.shirongbao.timenest.common.enums.ProcessingStatusEnum;
import com.shirongbao.timenest.common.enums.StatusEnum;
import com.shirongbao.timenest.dao.FriendshipsMapper;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.entity.FriendRequests;
import com.shirongbao.timenest.pojo.entity.Friendships;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import com.shirongbao.timenest.service.FriendRequestsService;
import com.shirongbao.timenest.service.FriendshipsService;
import com.shirongbao.timenest.service.NotificationService;
import com.shirongbao.timenest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    private final UserService userService;

    private final FriendRequestsService friendRequestsService;

    private final NotificationService notificationService;

    @Override
    public List<UsersVo> getFriendList(long currentUserId) throws ExecutionException, InterruptedException {
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

        // 组装usersVo
        return combineUserVo(friendshipsList1, currentUserId);
    }

    private List<UsersVo> combineUserVo(List<Friendships> friendshipsList, Long currentUserId) {
        if (friendshipsList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UsersVo> usersVoList = new ArrayList<>();

        // 拿到所有的futures
        List<CompletableFuture<List<UsersVo>>> futures = new ArrayList<>();
        for (Friendships friendships : friendshipsList) {
            // 异步去执行
            CompletableFuture<List<UsersVo>> future = CompletableFuture.supplyAsync(() -> {
                UsersVo usersVo = new UsersVo();
                String userAccount;
                Long userId;
                // 这里是拿到好友的账号
                if (Objects.equals(friendships.getUserId1(), currentUserId)) {
                    userAccount = friendships.getUserAccount2();
                    userId = friendships.getUserId2();
                } else {
                    userAccount = friendships.getUserAccount1();
                    userId = friendships.getUserId1();
                }
                usersVo.setUserAccount(userAccount);
                // 拿到好友的信息并返回
                Users friendUser = userService.getById(userId);
                usersVo.setId(userId);
                usersVo.setUserAccount(userAccount);
                usersVo.setNickName(friendUser.getNickName());
                usersVo.setAvatarUrl(friendUser.getAvatarUrl());
                usersVo.setIntroduce(friendUser.getIntroduce());
                usersVoList.add(usersVo);
                return usersVoList;
            }, labelThreadPool);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();

        // 执行完就可以返回了
        return usersVoList;
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

    @Override
    public Result<String> sendFriendRequest(UsersBo usersBo) {
        String userAccount = usersBo.getUserAccount();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserAccount, userAccount);
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.eq(Users::getStatus, StatusEnum.NORMAL.getCode());
        Users users = userService.getOne(wrapper);
        if (ObjectUtils.isEmpty(users)) {
            throw new RuntimeException("当前用户账号异常，添加失败~");
        }

        // 要先判断这个用户是否是我的好友
        boolean isFriend = checkIsFriend(users.getId());
        if (isFriend) {
            throw new RuntimeException("你们已经是好友啦，不需要再添加了~");
        }

        Long receiverUserId = users.getId();
        long senderUserId = StpUtil.getLoginIdAsLong();

        // 记录好友请求表
        FriendRequests friendRequests = new FriendRequests();
        friendRequests.setSenderUserId(senderUserId);
        friendRequests.setReceiverUserId(receiverUserId);
        friendRequests.setRequestMessage(usersBo.getRequestMessage());

        // 查询是否已经有未处理的请求
        LambdaQueryWrapper<FriendRequests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequests::getSenderUserId, senderUserId);
        queryWrapper.eq(FriendRequests::getReceiverUserId, receiverUserId);
        queryWrapper.eq(FriendRequests::getProcessingStatus, ProcessingStatusEnum.WAITING.getCode());
        queryWrapper.eq(FriendRequests::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        FriendRequests one = friendRequestsService.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(one)) {
            return Result.success("当前已发送过好友申请,请勿再次发送!");
        }

        friendRequestsService.save(friendRequests);

        // 记录通知表
        notificationService.saveNotification(friendRequests.getId(), receiverUserId, senderUserId);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Boolean> processingFriendRequest(Long friendRequestId, Integer processingResult) {
        LambdaQueryWrapper<FriendRequests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequests::getId, friendRequestId);
        queryWrapper.eq(FriendRequests::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        FriendRequests friendRequests = friendRequestsService.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(friendRequests)) {
            throw new RuntimeException("当前好友请求不存在，请刷新后重试！");
        }

        friendRequests.setProcessingStatus(ProcessingStatusEnum.PASSING.getCode());
        friendRequests.setProcessingResult(ProcessingResultEnum.REFUSE.getCode());

        if (processingResult == ProcessingResultEnum.ACCEPT.getCode()) {
            friendRequests.setProcessingResult(ProcessingResultEnum.ACCEPT.getCode());

            // 往好友表里插入数据
            Friendships friendships = new Friendships();
            Long user1Id = friendRequests.getSenderUserId();
            friendships.setUserId1(user1Id);
            Long user2Id = friendRequests.getReceiverUserId();
            friendships.setUserId2(user2Id);

            Users users1 = userService.getById(user1Id);
            Users users2 = userService.getById(user2Id);

            friendships.setUserAccount1(users1.getUserAccount());
            friendships.setUserAccount2(users2.getUserAccount());
            save(friendships);
        }

        friendRequestsService.updateById(friendRequests);
        return Result.success();
    }
}
