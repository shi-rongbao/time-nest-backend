package com.shirongbao.timenest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.FriendshipsMapper;
import com.shirongbao.timenest.pojo.entity.Friendships;
import com.shirongbao.timenest.service.FriendshipsService;
import org.springframework.stereotype.Service;

/**
  * @author: ShiRongbao
  * @date: 2025-05-19
  * @description: 好友关系服务实现类
  */
@Service("friendshipsService")
public class FriendshipsServiceImpl extends ServiceImpl<FriendshipsMapper, Friendships> implements FriendshipsService {
}
