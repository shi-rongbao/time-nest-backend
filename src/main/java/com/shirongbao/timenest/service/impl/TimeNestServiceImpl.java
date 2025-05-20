package com.shirongbao.timenest.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.PublicStatusEnum;
import com.shirongbao.timenest.common.enums.StatusEnum;
import com.shirongbao.timenest.common.enums.UnlockedStatusEnum;
import com.shirongbao.timenest.converter.TimeNestConverter;
import com.shirongbao.timenest.dao.TimeNestMapper;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.service.TimeNestService;
import com.shirongbao.timenest.strategy.NestStrategy;
import com.shirongbao.timenest.strategy.NestStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪服务实现类
 */
@Service("timeNestService")
@RequiredArgsConstructor
public class TimeNestServiceImpl extends ServiceImpl<TimeNestMapper, TimeNest> implements TimeNestService {

    private final NestStrategyFactory nestStrategyFactory;

    @Override
    public List<TimeNestBo> queryMyUnlockingNestList() {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeNest::getUserId, userId);
        wrapper.eq(TimeNest::getUnlockedStatus, UnlockedStatusEnum.LOCK.getCode());
        wrapper.eq(TimeNest::getNestStatus, StatusEnum.NORMAL.getCode());
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        // 根据解锁日期排序
        wrapper.orderByAsc(TimeNest::getUnlockTime);
        // 最多要4条即可！
        wrapper.last("limit 4");

        List<TimeNest> timeNestList = list(wrapper);
        if (CollectionUtils.isEmpty(timeNestList)) {
            return Collections.emptyList();
        }

        // 转成boList
        List<TimeNestBo> timeNestBoList = TimeNestConverter.INSTANCE.tineNestListToTimeNestBoList(timeNestList);
        for (TimeNestBo timeNestBo : timeNestBoList) {
            // 计算还有几天解锁
            int unlockDays = (int) ((timeNestBo.getUnlockTime().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));
            // 别出现0天这种情况！
            unlockDays += 1;
            timeNestBo.setUnlockDays(unlockDays);
        }
        return timeNestBoList;
    }

    @Override
    public void unlockNest(Long nestId) {
        // 先拿到这个nest
        TimeNest timeNest = getById(nestId);
        if (timeNest == null || timeNest.getUserId() != StpUtil.getLoginIdAsLong()) {
            throw new RuntimeException("当前数据异常，请稍后再试！");
        }

        // 设置解锁信息
        timeNest.setUnlockedStatus(UnlockedStatusEnum.UNLOCK.getCode());
        Integer publicStatus = timeNest.getPublicStatus();
        if (publicStatus == PublicStatusEnum.PUBLIC.getCode()) {
            timeNest.setPublicTime(new Date());
        }

        updateById(timeNest);

        // todo 后面有了公开表，要修改公开表的内容

        // 根据不同的type，执行不同的策略
        Integer capsuleType = timeNest.getNestType();
        NestStrategy strategy = nestStrategyFactory.getStrategy(capsuleType);
        strategy.unlockTimeNest(timeNest);
    }

    @Override
    public void createTimeNest(TimeNestBo timeNestBo) {
        TimeNest timeNest = TimeNestConverter.INSTANCE.timeNestBoToTimeNest(timeNestBo);
        timeNest.setUserId(StpUtil.getLoginIdAsLong());

        // 设置共同发布好友id
        List<Long> friendIdList = timeNestBo.getFriendIdList();
        if (CollectionUtils.isEmpty(friendIdList)) {
            friendIdList = new ArrayList<>();
        }
        friendIdList = friendIdList.stream().sorted().collect(Collectors.toList());
        String friendIds = JSON.toJSONString(friendIdList);
        timeNest.setFriendIds(friendIds);

        // 设置解锁通知用户id
        List<Long> unlockToUserIdList = timeNestBo.getUnlockToUserIdList();
        if (CollectionUtils.isEmpty(unlockToUserIdList)) {
            unlockToUserIdList = new ArrayList<>();
        }
        // 邀请好友默认也会通知
        List<Long> mergedList = Stream.concat(friendIdList.stream(), unlockToUserIdList.stream())
                .distinct()
                .sorted()
                .toList();

        // 如果没有选择解锁人，默认自己和好友都解锁
        unlockToUserIdList.add(StpUtil.getLoginIdAsLong());
        unlockToUserIdList.addAll(mergedList);
        // 去重
        unlockToUserIdList = unlockToUserIdList.stream().distinct().sorted().collect(Collectors.toList());
        String unlockToUserIds = JSON.toJSONString(unlockToUserIdList);
        timeNest.setUnlockToUserIds(unlockToUserIds);
        timeNest.setUnlockedStatus(UnlockedStatusEnum.LOCK.getCode());

        // 执行对应的策略,完成个性化的创建
        Integer nestType = timeNestBo.getNestType();
        NestStrategy strategy = nestStrategyFactory.getStrategy(nestType);
        timeNest = strategy.createTimeNest(timeNest);
        save(timeNest);
    }

}
