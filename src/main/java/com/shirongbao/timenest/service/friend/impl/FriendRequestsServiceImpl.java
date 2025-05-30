package com.shirongbao.timenest.service.friend.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.FriendRequestsMapper;
import com.shirongbao.timenest.pojo.entity.FriendRequests;
import com.shirongbao.timenest.service.friend.FriendRequestsService;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友请求服务实现类
 */
@Service("friendRequestsService")
public class FriendRequestsServiceImpl extends ServiceImpl<FriendRequestsMapper, FriendRequests> implements FriendRequestsService {
}
