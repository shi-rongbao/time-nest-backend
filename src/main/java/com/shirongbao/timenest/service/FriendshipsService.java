package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.Friendships;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友关系服务接口
 */
public interface FriendshipsService extends IService<Friendships> {

    // 查看当前用户好友列表
    List<Friendships> getFriendList(long currentUserId) throws ExecutionException, InterruptedException;

    // 检查当前用户是否是我的好友
    boolean checkIsFriend(Long id);
}
