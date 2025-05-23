package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.entity.Friendships;
import com.shirongbao.timenest.pojo.vo.UsersVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友关系服务接口
 */
public interface FriendshipsService extends IService<Friendships> {

    // 查看当前用户好友列表
    List<UsersVo> getFriendList(long currentUserId) throws ExecutionException, InterruptedException;

    // 检查当前用户是否是我的好友
    boolean checkIsFriend(Long id);

    // 发送好友申请
    Result<String> sendFriendRequest(UsersBo usersBo);

    // 处理好友申请
    Result<Boolean> processingFriendRequest(Long friendRequestId, Integer processingResult);
}
